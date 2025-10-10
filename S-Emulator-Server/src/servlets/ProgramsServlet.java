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

            boolean firstProgram = true;
            for (Map.Entry<String, S_Emulator> entry : allPrograms.entrySet()) {
                if (!firstProgram) {
                    jsonBuilder.append(",");
                }

                String compositeKey = entry.getKey(); // This is userId_programName
                S_Emulator emulator = entry.getValue();

                // Extract userId and programName from composite key
                String[] keyParts = compositeKey.split("_", 2);
                String userId = keyParts[0];
                String programName = keyParts.length > 1 ? keyParts[1] : "Unknown";

                // Extract actual program information from S_Emulator
                String actualProgramName = emulator.getCurrentProgramName();
                int numInstructions = emulator.getCommandsAtDesiredLevel(0).size();
                int maxDegree = emulator.getMaxExpansionDepth();
                int numExecutions = emulator.getExecutionHistory().getExecutionCount();
                double avgCreditCost = emulator.getExecutionHistory().getAverageCreditCost();

                // Use actual program name from emulator if available, otherwise use extracted name
                String displayProgramName = (actualProgramName != null && !actualProgramName.trim().isEmpty()) ?
                                          actualProgramName : programName;

                jsonBuilder.append("{");
                jsonBuilder.append("\"programName\":\"").append(escapeJson(displayProgramName)).append("\",");
                jsonBuilder.append("\"uploaderName\":\"").append(escapeJson(userId)).append("\",");
                jsonBuilder.append("\"numOfInstructions\":").append(numInstructions).append(",");
                jsonBuilder.append("\"maxDegree\":").append(maxDegree).append(",");
                jsonBuilder.append("\"numOfExecutions\":").append(numExecutions).append(",");
                jsonBuilder.append("\"avgCreditCost\":").append(avgCreditCost);
                jsonBuilder.append("}");

                firstProgram = false;
            }

            jsonBuilder.append("],\"functions\":[");

            // Add functions from all programs
            boolean firstFunction = true;
            for (Map.Entry<String, S_Emulator> entry : allPrograms.entrySet()) {
                String compositeKey = entry.getKey();
                S_Emulator emulator = entry.getValue();

                // Extract userId and programName from composite key
                String[] keyParts = compositeKey.split("_", 2);
                String userId = keyParts[0];
                String programName = keyParts.length > 1 ? keyParts[1] : "Unknown";

                // Get sub-functions from the program
                if (emulator instanceof engine.Program) {
                    engine.Program program = (engine.Program) emulator;
                    if (program.subFunctions != null) {
                        for (engine.Program subFunction : program.subFunctions) {
                            if (!firstFunction) {
                                jsonBuilder.append(",");
                            }

                            String functionName = subFunction.getCurrentProgramName();
                            if (functionName == null || functionName.trim().isEmpty()) {
                                functionName = "Function_" + System.currentTimeMillis();
                            }

                            int functionInstructions = subFunction.getCommandsAtDesiredLevel(0).size();
                            int functionMaxDegree = subFunction.getMaxExpansionDepth();

                            jsonBuilder.append("{");
                            jsonBuilder.append("\"functionName\":\"").append(escapeJson(functionName)).append("\",");
                            jsonBuilder.append("\"associatedProgram\":\"").append(escapeJson(programName)).append("\",");
                            jsonBuilder.append("\"associatedUser\":\"").append(escapeJson(userId)).append("\",");
                            jsonBuilder.append("\"numOfInstructions\":").append(functionInstructions).append(",");
                            jsonBuilder.append("\"maxDegree\":").append(functionMaxDegree);
                            jsonBuilder.append("}");

                            firstFunction = false;
                        }
                    }
                }
            }

            jsonBuilder.append("]}");

            resp.getWriter().write(jsonBuilder.toString());
            System.out.println("SERVER - Sent programs and functions data: " + jsonBuilder.toString());

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