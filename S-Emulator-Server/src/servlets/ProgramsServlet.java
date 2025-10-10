package servlets;

import engine.S_Emulator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/programs")
public class ProgramsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ServerContext context = ServerContext.getInstance();

            // Get all stored programs
            Map<String, S_Emulator> allPrograms = context.getAllStoredPrograms();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            // Manually build JSON response
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"programs\":[");

            boolean first = true;
            for (Map.Entry<String, S_Emulator> entry : allPrograms.entrySet()) {
                if (!first) {
                    jsonBuilder.append(",");
                }

                String userId = entry.getKey();
                S_Emulator emulator = entry.getValue();

                // Extract program information from S_Emulator
                String programName = emulator.getCurrentProgramName(); // You need this method
                int numInstructions = emulator.getCommandsAtDesiredLevel(0).size(); // You need this method
                int maxDegree = emulator.getMaxExpansionDepth(); // You need this method
                int numExecutions = emulator.getExecutionCount(); // You need this method
                double avgCreditCost = emulator.getAverageCreditCost(); // You need this method

                jsonBuilder.append("{");
                jsonBuilder.append("\"programName\":\"").append(escapeJson(programName)).append("\",");
                jsonBuilder.append("\"uploaderName\":\"").append(escapeJson(userId)).append("\",");
                jsonBuilder.append("\"numOfInstructions\":").append(numInstructions).append(",");
                jsonBuilder.append("\"maxDegree\":").append(maxDegree).append(",");
                jsonBuilder.append("\"numOfExecutions\":").append(numExecutions).append(",");
                jsonBuilder.append("\"avgCreditCost\":").append(avgCreditCost);
                jsonBuilder.append("}");

                first = false;
            }

            jsonBuilder.append("]}");

            resp.getWriter().write(jsonBuilder.toString());
            System.out.println("SERVER - Sent programs data: " + jsonBuilder.toString());

        } catch (Exception e) {
            System.err.println("SERVER - Error in ProgramsServlet: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "null";
        }

        return input.replace("\\", "\\\\")    // Escape backslashes first
                .replace("\"", "\\\"")     // Escape quotes
                .replace("\b", "\\b")      // Escape backspace
                .replace("\f", "\\f")      // Escape form feed
                .replace("\n", "\\n")      // Escape newline
                .replace("\r", "\\r")      // Escape carriage return
                .replace("\t", "\\t");     // Escape tab
    }
}