package servlets;

import api.dto.ExecutionDetailsDTO;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet("/executionDetails")
public class ExecutionDetailsServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String runIdParam = req.getParameter("runId");
        String userIdParam = req.getParameter("userId");

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json");

        if (runIdParam == null || runIdParam.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GSON.toJson("Missing runId parameter"));
            return;
        }

        int runId;
        try {
            runId = Integer.parseInt(runIdParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GSON.toJson("Invalid runId parameter"));
            return;
        }

        try {
            ServerContext context = ServerContext.getInstance();
            User targetUser;

            if (userIdParam != null && !userIdParam.trim().isEmpty()) {
                // Get execution details for specified user
                targetUser = context.getUser(userIdParam);
                if (targetUser == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(GSON.toJson("User not found: " + userIdParam));
                    return;
                }
            } else {
                // Get execution details for current logged-in user
                HttpSession session = req.getSession();
                String currentUsername = (String) session.getAttribute("username");

                if (currentUsername == null) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write(GSON.toJson("User not logged in"));
                    return;
                }

                targetUser = context.getUser(currentUsername);
                if (targetUser == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(GSON.toJson("Current user not found"));
                    return;
                }
            }

            ExecutionDetailsDTO details = targetUser.getExecutionDetails(runId);
            if (details == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(GSON.toJson("Execution details not found for runId: " + runId));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(GSON.toJson(details));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(GSON.toJson("Error retrieving execution details"));
        }
    }
}

