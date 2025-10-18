package servlets;

import api.dto.ChatMessageDTO;
import api.dto.ChatMessagesResponseDTO;
import api.dto.PostChatMessageRequest;
import api.dto.PostChatMessageResponse;
import com.google.gson.Gson;
import engine.chat.ChatManager;
import engine.chat.SingleChatEntry;
import utils.SessionUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ChatServlet", urlPatterns = {"/api/chat/messages"})
public class ChatServlet extends HttpServlet {
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MIN_MESSAGE_LENGTH = 1;
    private static final int HTTP_TOO_MANY_REQUESTS = 429; // HTTP 429 status code

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();

        try {
            // Get the 'since' parameter (ISO-8601 timestamp)
            String sinceParam = request.getParameter("since");

            ServerContext context = ServerContext.getInstance();
            ChatManager chatManager = context.getChatManager();

            // Fetch messages since the specified timestamp
            List<SingleChatEntry> entries;
            if (sinceParam != null && !sinceParam.isEmpty()) {
                entries = chatManager.getChatEntriesSince(sinceParam);
            } else {
                // If no 'since' parameter, return all messages (initial load)
                entries = chatManager.getChatEntries(0);
            }

            // Convert to DTOs
            List<ChatMessageDTO> messageDTOs = new ArrayList<>();
            for (SingleChatEntry entry : entries) {
                ChatMessageDTO dto = new ChatMessageDTO(
                    entry.getMessageId(),
                    entry.getUserId(),
                    entry.getUsername(),
                    entry.getChatString(),
                    entry.getTimestamp()
                );
                messageDTOs.add(dto);
            }

            // Create response with nextSince timestamp (current time for next poll)
            String nextSince = Instant.now().toString();
            ChatMessagesResponseDTO responseDTO = new ChatMessagesResponseDTO(messageDTOs, nextSince);

            String jsonResponse = gson.toJson(responseDTO);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(createErrorResponse("Failed to fetch chat messages: " + e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();

        try {
            // Check if user is logged in
            String userId = SessionUtils.getUserId(request);
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(createErrorResponse("User not logged in")));
                return;
            }

            // Get username from session or context
            String username = SessionUtils.getUsername(request);
            if (username == null) {
                username = userId; // Fall back to userId if username not found
            }

            // Parse request body
            PostChatMessageRequest postRequest = gson.fromJson(request.getReader(), PostChatMessageRequest.class);

            if (postRequest == null || postRequest.getText() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(createErrorResponse("Missing text field in request")));
                return;
            }

            String text = postRequest.getText().trim();

            // Validate message text
            if (text.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(createErrorResponse("Message text cannot be empty")));
                return;
            }

            if (text.length() > MAX_MESSAGE_LENGTH) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(createErrorResponse("Message text exceeds maximum length of " + MAX_MESSAGE_LENGTH + " characters")));
                return;
            }

            // Sanitize text to prevent injection
            text = sanitizeText(text);

            // Add message to chat
            ServerContext context = ServerContext.getInstance();
            ChatManager chatManager = context.getChatManager();

            try {
                SingleChatEntry entry = chatManager.addChatMessage(userId, text, username);

                // Create response
                PostChatMessageResponse postResponse = new PostChatMessageResponse(
                    entry.getMessageId(),
                    entry.getTimestamp()
                );

                String jsonResponse = gson.toJson(postResponse);
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(jsonResponse);

                System.out.println("User '" + username + "' posted message: " + text);

            } catch (IllegalStateException e) {
                // Rate limit exceeded
                response.setStatus(HTTP_TOO_MANY_REQUESTS);
                response.getWriter().write(gson.toJson(createErrorResponse(e.getMessage())));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(createErrorResponse("Failed to post chat message: " + e.getMessage())));
        }
    }

    private String sanitizeText(String text) {
        // Basic sanitization to prevent HTML/script injection
        return text.replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private ErrorResponse createErrorResponse(String message) {
        return new ErrorResponse(message);
    }

    private static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}
