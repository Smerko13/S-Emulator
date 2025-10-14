package servlets;

import api.dto.ExecutionHistoryDTO;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/executionHistory")
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
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(GSON.toJson("User not found: " + targetUserId));
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
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(GSON.toJson("Current user not found"));
                    return;
                }
                history = currentUser.getExecutionHistory();
                System.out.println("ExecutionHistoryServlet: Retrieved " + history.size() + " records for current user: " + currentUsername);
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(GSON.toJson(history));

        } catch (Exception e) {
            System.err.println("Error in ExecutionHistoryServlet: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(GSON.toJson("Error retrieving execution history"));
        }
    }
}
