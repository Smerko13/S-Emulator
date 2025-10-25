package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/addCredits")
public class AddCreditsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);

        if (session == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Not logged in\"}");
            return;
        }

        String username = (String) session.getAttribute("username");

        if (username == null || username.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"User not logged in\"}");
            return;
        }

        String creditsParam = req.getParameter("credits");

        if (creditsParam == null || creditsParam.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Credits amount is required\"}");
            return;
        }

        try {
            int creditsToAdd = Integer.parseInt(creditsParam);

            if (creditsToAdd <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Credits amount must be positive\"}");
                return;
            }

            ServerContext context = ServerContext.getInstance();
            User user = context.getUser(username);

            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"User not found\"}");
                return;
            }

            user.addCredits(creditsToAdd);
            int newCredits = user.getCredits();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"success\":true,\"newCredits\":" + newCredits + "}");

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Invalid credits amount\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
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

