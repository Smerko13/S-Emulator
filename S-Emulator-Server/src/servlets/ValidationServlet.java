package servlets;

import engine.Program;
import engine.S_Emulator;
import engine.commands.Command;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet("/validate")
public class ValidationServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        byte[] requestBytes = req.getInputStream().readAllBytes();
        String xmlContent = new String(requestBytes, StandardCharsets.UTF_8);

        String userId = req.getParameter("userId");

        if (userId == null || userId.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("User ID is required");
            return;
        }

        // Validate and process the XML content
        ValidationResult result = validateAndProcessXml(xmlContent, userId);

        if (!result.isValid()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Validation failed: " + result.getErrorMessage());
            return;
        }

        // Success
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("XML is valid and program stored with name: " + result.getProgramName());
    }

    private ValidationResult validateAndProcessXml(String xmlContent, String username) {
        try {
            // Parse the XML content
            Program program = new Program(false);
            boolean parseSuccess = program.readProgramFromXml(xmlContent);

            if (!parseSuccess) {
                return ValidationResult.error("Invalid XML format or program structure");
            }

            String programName = program.getCurrentProgramName();
            if (programName == null || programName.trim().isEmpty()) {
                return ValidationResult.error("Program must have a valid name");
            }

            // Check if main program name is unique across all users
            ServerContext context = ServerContext.getInstance();
            Map<String, S_Emulator> allPrograms = context.getAllStoredPrograms();

            for (Map.Entry<String, S_Emulator> entry : allPrograms.entrySet()) {
                S_Emulator existingProgram = entry.getValue();
                if (existingProgram.getCurrentProgramName() != null &&
                    existingProgram.getCurrentProgramName().equals(programName)) {
                    return ValidationResult.error("A program with name '" + programName + "' already exists in the system");
                }
            }

            // Check for duplicate function names across all users
            Set<String> existingFunctions = getAllExistingFunctionNames();
            List<String> newFunctions = extractFunctionNames(program);

            for (String functionName : newFunctions) {
                if (existingFunctions.contains(functionName)) {
                    return ValidationResult.error("Function '" + functionName + "' already exists in the system");
                }
            }

            // Check if program references only existing functions
            Set<String> referencedFunctions = extractReferencedFunctions(program);
            Set<String> availableFunctions = new HashSet<>(existingFunctions);
            availableFunctions.addAll(newFunctions); // Include functions from this file

            for (String referencedFunction : referencedFunctions) {
                if (!availableFunctions.contains(referencedFunction)) {
                    return ValidationResult.error("Program references undefined function: '" + referencedFunction + "'");
                }
            }

            // All validations passed - store the program
            context.storeUserProgram(username, programName, program);
            return ValidationResult.success(programName, newFunctions);

        } catch (Exception e) {
            return ValidationResult.error("Error processing XML: " + e.getMessage());
        }
    }

    private Set<String> getAllExistingFunctionNames() {
        ServerContext context = ServerContext.getInstance();
        Set<String> functionNames = new HashSet<>();

        // Get all programs from all users
        Map<String, S_Emulator> allPrograms = context.getAllStoredPrograms();

        for (S_Emulator emulator : allPrograms.values()) {
            if (emulator instanceof Program) {
                Program program = (Program) emulator;

                // Add main program name as a function
                if (program.getCurrentProgramName() != null) {
                    functionNames.add(program.getCurrentProgramName());
                }

                // Add all sub-functions
                for (Program subFunction : program.subFunctions) {
                    if (subFunction.getCurrentProgramName() != null) {
                        functionNames.add(subFunction.getCurrentProgramName());
                    }
                }
            }
        }

        return functionNames;
    }

    private List<String> extractFunctionNames(Program program) {
        List<String> functionNames = new ArrayList<>();

        // Add main program name
        if (program.getCurrentProgramName() != null) {
            functionNames.add(program.getCurrentProgramName());
        }

        // Add sub-function names
        for (Program subFunction : program.subFunctions) {
            if (subFunction.getCurrentProgramName() != null) {
                functionNames.add(subFunction.getCurrentProgramName());
            }
        }

        return functionNames;
    }

    private Set<String> extractReferencedFunctions(Program program) {
        Set<String> referencedFunctions = new HashSet<>();

        try {
            // Get all commands from the program at all expansion levels
            List<Command> allCommands = program.getCommands();

            for (Command command : allCommands) {
                // Check if this is a QUOTE command (function call)
                if (command instanceof engine.commands.synthetic.types.Quote) {
                    engine.commands.synthetic.types.Quote quoteCommand =
                        (engine.commands.synthetic.types.Quote) command;

                    // Extract the main function name using public getter
                    if (quoteCommand.getFunctionName() != null && !quoteCommand.getFunctionName().trim().isEmpty()) {
                        referencedFunctions.add(quoteCommand.getFunctionName());
                    }

                    // Extract nested function calls from arguments
                    if (quoteCommand.getArgumentList() != null) {
                        for (String arg : quoteCommand.getArgumentList()) {
                            if (arg != null && arg.trim().startsWith("(") && arg.trim().endsWith(")")) {
                                // This is a nested function call
                                extractFunctionNamesFromArguments(arg, referencedFunctions);
                            }
                        }
                    }
                }
            }

            // Also check sub-functions for their own function references
            for (Program subFunction : program.subFunctions) {
                referencedFunctions.addAll(extractReferencedFunctions(subFunction));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return referencedFunctions;
    }

    private void extractFunctionNamesFromArguments(String functionArguments, Set<String> referencedFunctions) {
        if (functionArguments == null || functionArguments.trim().isEmpty()) {
            return;
        }

        // Handle nested function calls like "(Minus,x1,x2)" or "(NOT,(EQUAL,x2,x1))"
        int parenthesesDepth = 0;
        StringBuilder currentFunction = new StringBuilder();
        boolean inFunction = false;

        for (char c : functionArguments.toCharArray()) {
            if (c == '(') {
                parenthesesDepth++;
                if (!inFunction) {
                    inFunction = true;
                    currentFunction.setLength(0);
                } else {
                    currentFunction.append(c);
                }
            } else if (c == ')') {
                parenthesesDepth--;
                if (parenthesesDepth == 0 && inFunction) {
                    // End of a function call, extract the function name
                    String functionCall = currentFunction.toString();
                    String functionName = extractFunctionNameFromCall(functionCall);
                    if (functionName != null && isValidFunctionName(functionName)) {
                        referencedFunctions.add(functionName);
                    }
                    inFunction = false;
                } else if (inFunction) {
                    currentFunction.append(c);
                }
            } else if (inFunction) {
                currentFunction.append(c);
            }
        }
    }

    private String extractFunctionNameFromCall(String functionCall) {
        if (functionCall == null || functionCall.trim().isEmpty()) {
            return null;
        }

        // Extract the function name (everything before the first comma or end of string)
        int commaIndex = functionCall.indexOf(',');
        if (commaIndex != -1) {
            return functionCall.substring(0, commaIndex).trim();
        } else {
            return functionCall.trim();
        }
    }

    private boolean isValidFunctionName(String name) {
        return name != null && !name.trim().isEmpty() &&
               name.matches("[a-zA-Z][a-zA-Z0-9_]*"); // Valid identifier pattern
    }

    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final String programName;
        private final List<String> functionsAdded;

        private ValidationResult(boolean valid, String errorMessage, String programName, List<String> functionsAdded) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.programName = programName;
            this.functionsAdded = functionsAdded != null ? functionsAdded : new ArrayList<>();
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, null, null);
        }

        public static ValidationResult success(String programName, List<String> functionsAdded) {
            return new ValidationResult(true, null, programName, functionsAdded);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public String getProgramName() { return programName; }
        public List<String> getFunctionsAdded() { return functionsAdded; }
    }
}