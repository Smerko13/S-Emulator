package servlets;

import api.dto.UserSummary;
import com.google.gson.Gson;
import engine.Program;
import engine.S_Emulator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/userslist")
public class UsersListServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ServerContext context = ServerContext.getInstance();
            Map<String, User> allUsers = context.getAllUsers();

            List<UserSummary> summaries = new ArrayList<>();
            for (User user : allUsers.values()) {
                UserSummary summary = createUserSummary(user);
                summaries.add(summary);

                System.out.println("UsersListServlet: User " + user.getUserName() +
                        " - Programs: " + summary.programs +
                        ", Functions: " + summary.functions +
                        ", Credits: " + summary.creditsAvailable +
                        ", Used: " + summary.creditsUsed +
                        ", Executions: " + summary.executions);
            }

            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(GSON.toJson(summaries));
        } catch (Exception e) {
            System.err.println("Error in UsersListServlet: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(GSON.toJson("Error retrieving user list"));
        }
    }

    private UserSummary createUserSummary(User user) {
        String username = user.getUserName();
        int currentCredits = user.getCredits();
        int creditsUsed = user.getTotalCreditsUsed();
        int totalExecutions = user.getTotalExecutions();

        // Count main programs and helper functions
        int mainPrograms = 0;
        int helperFunctions = 0;

        Map<String, S_Emulator> userPrograms = user.getAllPrograms();

        for (S_Emulator emulator : userPrograms.values()) {
            if (emulator instanceof Program) {
                Program program = (Program) emulator;
                mainPrograms++;
                helperFunctions += program.subFunctions.size();
            }
        }

        return new UserSummary(username, mainPrograms, helperFunctions,
                currentCredits, creditsUsed, totalExecutions);
    }
}