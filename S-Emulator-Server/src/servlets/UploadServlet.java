package servlets;

import api.dto.ProgramInfoDTO;
import com.google.gson.Gson;
import engine.Program;
import engine.S_Emulator;
import engine.arguments.Variable;
import engine.commands.Command;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import schema.SProgram;
import schema.SFunction;
import schema.SFunctions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 5,   // 5 MB
    maxRequestSize = 1024 * 1024 * 10 // 10 MB
)
public class UploadServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(GSON.toJson("User not logged in"));
            return;
        }

        try {
            // Get the uploaded file part
            Part filePart = request.getPart("file");
            if (filePart == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(GSON.toJson("No file uploaded"));
                return;
            }

            // Validate file type
            String fileName = getFileName(filePart);
            if (!fileName.toLowerCase().endsWith(".xml")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(GSON.toJson("Only XML files are allowed"));
                return;
            }

            // Read file content without storing it
            String xmlContent;
            try (InputStream inputStream = filePart.getInputStream()) {
                xmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            // Validate and process the XML content
            ValidationResult result = validateAndProcessXml(xmlContent, username);

            if (!result.isValid()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(GSON.toJson("Validation failed: " + result.getErrorMessage()));
                return;
            }

            // Success response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "File uploaded and processed successfully");
            successResponse.put("programName", result.getProgramName());
            successResponse.put("functionsAdded", result.getFunctionsAdded());

            response.getWriter().write(GSON.toJson(successResponse));

        } catch (Exception e) {
            System.err.println("Upload error: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON.toJson("Upload failed: " + e.getMessage()));
        }
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] tokens = contentDisposition.split(";");

        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
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

            // Check if main program name is unique
            ServerContext context = ServerContext.getInstance();
            if (context.hasUserProgram(username, programName)) {
                return ValidationResult.error("A program with name '" + programName + "' already exists");
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
                // Check if this is a QUOTE command specifically
                if (command instanceof engine.commands.synthetic.types.Quote) {
                    engine.commands.synthetic.types.Quote quoteCommand =
                        (engine.commands.synthetic.types.Quote) command;

                    // Extract the main function name using public getter
                    if (quoteCommand.getFunctionName() != null && !quoteCommand.getFunctionName().trim().isEmpty()) {
                        referencedFunctions.add(quoteCommand.getFunctionName());
                    }

                    // Extract function names from the arguments using public getter
                    if (quoteCommand.getFunctionArguments() != null && !quoteCommand.getFunctionArguments().trim().isEmpty()) {
                        extractFunctionNamesFromArguments(quoteCommand.getFunctionArguments(), referencedFunctions);
                    }

                    // Also check the argument list for nested function calls using public getter
                    if (quoteCommand.getArgumentList() != null) {
                        for (String arg : quoteCommand.getArgumentList()) {
                            if (arg != null && arg.trim().startsWith("(") && arg.trim().endsWith(")")) {
                                // This is a nested function call
                                extractFunctionNamesFromArguments(arg, referencedFunctions);
                            }
                        }
                    }
                }

                // Handle other command types that might reference functions
                String commandType = command.getClass().getSimpleName().toUpperCase();
                if (commandType.equals("JUMPEQUALFUNCTION") || commandType.equals("JUMP_EQUAL_FUNCTION")) {
                    // Extract function name from command string representation
                    String commandStr = command.toString();
                    if (commandStr != null) {
                        extractFunctionNamesFromCommandString(commandStr, referencedFunctions);
                    }
                }
            }

            // Also check sub-functions for their own function references
            for (Program subFunction : program.subFunctions) {
                referencedFunctions.addAll(extractReferencedFunctions(subFunction));
            }

        } catch (Exception e) {
            System.err.println("Error extracting function references: " + e.getMessage());
            e.printStackTrace();
        }

        return referencedFunctions;
    }

    private void extractFunctionNamesFromArguments(String functionArguments, Set<String> referencedFunctions) {
        if (functionArguments == null || functionArguments.trim().isEmpty()) {
            return;
        }

        // Handle nested function calls like "(Minus,x1,x2)" or "(NOT,(EQUAL,x2,x1))"
        int i = 0;
        StringBuilder currentFunction = new StringBuilder();
        boolean inFunction = false;
        int parenthesesDepth = 0;

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
        // Updated validation for function names to allow underscores and be more permissive
        return name != null && !name.trim().isEmpty() &&
               name.matches("[a-zA-Z][a-zA-Z0-9_]*"); // Valid identifier pattern with underscores
    }

    private void extractFunctionNamesFromCommandString(String commandStr, Set<String> referencedFunctions) {
        // This method parses the command string to find function references
        // Updated to handle the actual format used by Quote commands

        if (commandStr == null || commandStr.trim().isEmpty()) {
            return;
        }

        // Look for patterns like "y <- (functionName,args)" or "y <- (functionName)"
        if (commandStr.contains("(") && commandStr.contains(")")) {
            int startParen = commandStr.indexOf('(');
            int endParen = commandStr.lastIndexOf(')');

            if (startParen < endParen && startParen != -1) {
                String functionCall = commandStr.substring(startParen + 1, endParen);

                // Extract the function name (everything before the first comma or end of string)
                String functionName = extractFunctionNameFromCall(functionCall);
                if (functionName != null && isValidFunctionName(functionName)) {
                    referencedFunctions.add(functionName);
                }

                // Also recursively extract any nested function calls
                extractFunctionNamesFromArguments("(" + functionCall + ")", referencedFunctions);
            }
        }
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
