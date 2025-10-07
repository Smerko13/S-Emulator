package servlets;

import api.dto.UserSummary;                 // ← from API module
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/userslist")
public class UsersListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var userManager = ServletUtils.getUserManager(getServletContext());
        var names = userManager.getUsers();

        // TODO: replace these 0s with real numbers from your userManager when available
        List<UserSummary> summaries = names.stream()
                .map(n -> new UserSummary(n, 0, 0, 0, 0, 0))
                .toList();

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(new Gson().toJson(summaries));
    }
}
