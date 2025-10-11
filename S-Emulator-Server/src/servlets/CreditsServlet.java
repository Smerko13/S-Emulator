package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/credits")
public class CreditsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get user's current credits
        String userId = req.getParameter("userId");

        if (userId == null || userId.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"User ID is required\"}");
            return;
        }

        try {
            ServerContext context = ServerContext.getInstance();
            int credits = context.getUserCredits(userId);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"credits\":" + credits + "}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Add credits to user
        String userId = req.getParameter("userId");
        String creditsParam = req.getParameter("credits");

        if (userId == null || userId.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"User ID is required\"}");
            return;
        }

        if (creditsParam == null || creditsParam.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Credits amount is required\"}");
            return;
        }

        try {
            int creditsToAdd = Integer.parseInt(creditsParam);

            if (creditsToAdd <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Credits amount must be positive\"}");
                return;
            }

            ServerContext context = ServerContext.getInstance();
            context.addUserCredits(userId, creditsToAdd);
            int newCredits = context.getUserCredits(userId);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"success\":true,\"newCredits\":" + newCredits + "}");

            System.out.println("SERVER - Added " + creditsToAdd + " credits to user: " + userId +
                             ". New total: " + newCredits);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid credits amount\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Set user's credits to specific amount
        String userId = req.getParameter("userId");
        String creditsParam = req.getParameter("credits");

        if (userId == null || userId.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"User ID is required\"}");
            return;
        }

        if (creditsParam == null || creditsParam.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Credits amount is required\"}");
            return;
        }

        try {
            int creditsToSet = Integer.parseInt(creditsParam);

            if (creditsToSet < 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Credits amount cannot be negative\"}");
                return;
            }

            ServerContext context = ServerContext.getInstance();
            context.setUserCredits(userId, creditsToSet);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"success\":true,\"newCredits\":" + creditsToSet + "}");

            System.out.println("SERVER - Set credits for user: " + userId + " to: " + creditsToSet);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid credits amount\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "null";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
