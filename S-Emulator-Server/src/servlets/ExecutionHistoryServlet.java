package servlets;

import api.dto.ExecutionHistoryDTO;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExecutionHistoryServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetUserId = req.getParameter("userId");

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json");

        try {
            ServerContext context = ServerContext.getInstance();
            List<ExecutionHistoryDTO> history;

            if (targetUserId != null && !targetUserId.trim().isEmpty()) {
                // Get execution history for specified user
                User targetUser = context.getUser(targetUserId);
                if (targetUser == null) {
                    // Return empty array for non-existent user (tolerates server restart)
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(GSON.toJson(new ExecutionHistoryDTO[0]));
                    System.out.println("ExecutionHistoryServlet: User not found (may be after restart): " + targetUserId);
                    return;
                }
                history = targetUser.getExecutionHistory();
                System.out.println("ExecutionHistoryServlet: Retrieved " + history.size() + " records for user: " + targetUserId);
            } else {
                // Get execution history for current logged-in user
                HttpSession session = req.getSession();
                String currentUsername = (String) session.getAttribute("username");

                if (currentUsername == null) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write(GSON.toJson("User not logged in"));
                    return;
                }

                User currentUser = context.getUser(currentUsername);
                if (currentUser == null) {
                    // Return empty array after restart instead of error
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(GSON.toJson(new ExecutionHistoryDTO[0]));
                    System.out.println("ExecutionHistoryServlet: Current user not found (may be after restart): " + currentUsername);
                    return;
                }
                history = currentUser.getExecutionHistory();
                System.out.println("ExecutionHistoryServlet: Retrieved " + history.size() + " records for current user: " + currentUsername);
            }

            // Generate JSON and compute ETag
            String jsonResponse = GSON.toJson(history);
            String etag = String.valueOf(jsonResponse.hashCode());

            // Check for cached ETag
            String clientETag = req.getHeader("If-None-Match");

            resp.setHeader("ETag", etag);
            resp.setHeader("Cache-Control", "no-cache");

            // Return 304 if data hasn't changed
            if (clientETag != null && clientETag.equals(etag)) {
                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                System.out.println("ExecutionHistoryServlet: Returning 304 Not Modified");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);

        } catch (Exception e) {
            System.err.println("Error in ExecutionHistoryServlet: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(GSON.toJson("Error retrieving execution history"));
        }
    }
}
