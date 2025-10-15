package servlets;

import api.dto.*;
import com.google.gson.Gson;
import engine.S_Emulator;
import engine.Program;
import engine.commands.Command;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "ExecutionServlet", urlPatterns = {"/exec/*"})
public class ExecutionServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Set response content type
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            switch (pathInfo) {
                case "/open":
                    handleOpen(request, response);
                    break;
                case "/execute":
                    handleExecute(request, response);
                    break;
                case "/degree":
                case "/setDegree":
                    handleSetDegree(request, response);
                    break;
                case "/selectFunction":
                    handleSelectFunction(request, response);
                    break;
                case "/newRun":
                    handleNewRun(request, response);
                    break;
                case "/debug":
                    handleDebug(request, response);
                    break;
                case "/parentChain":
                    handleGetParentChain(request, response);
                    break;
                case "/updateInput":
                    handleUpdateInput(request, response);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON.toJson("Error: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // POST requests use the same handling logic as GET requests
        doGet(request, response);
    }

    private void handleOpen(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String target = request.getParameter("target");
        System.out.println("ExecutionServlet: handleOpen called with target: " + target);

        if (target == null || target.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("Missing target parameter"));
            return;
        }

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        System.out.println("ExecutionServlet: Username from session: " + username);

        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(GSON.toJson("User not logged in"));
            return;
        }

        try {
            // Create or get the engine for this user/program combination
            S_Emulator engine = getOrCreateEngine(session, target);
            System.out.println("ExecutionServlet: Engine loaded: " + (engine != null));

            // Create execution state DTO
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, target);
            System.out.println("ExecutionServlet: ExecutionStateDTO created with " +
                (executionState.getInstructions() != null ? executionState.getInstructions().size() : "null") +
                " instructions and " +
                (executionState.getAllVariables() != null ? executionState.getAllVariables().size() : "null") +
                " variables");

            // Store current program/function in session
            session.setAttribute("currentTarget", target);

            response.getWriter().write(GSON.toJson(executionState));

        } catch (Exception e) {
            System.err.println("ExecutionServlet: Error in handleOpen: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON.toJson("Failed to open program: " + e.getMessage()));
        }
    }

    private void handleExecute(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        S_Emulator engine = (S_Emulator) session.getAttribute("engine");
        String currentTarget = (String) session.getAttribute("currentTarget");

        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(GSON.toJson("User not logged in"));
            return;
        }

        if (engine == null || currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active execution session"));
            return;
        }

        try {
            // Parse the request body to get architecture information
            ExecuteProgramRequest execRequest = null;
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = request.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                if (requestBody.length() > 0) {
                    System.out.println("ExecutionServlet: Received execution request body: " + requestBody.toString());
                    execRequest = GSON.fromJson(requestBody.toString(), ExecuteProgramRequest.class);
                }
            }

            // Default to Generation I if no architecture specified
            Architecture architecture = (execRequest != null && execRequest.architecture != null)
                ? execRequest.architecture
                : Architecture.GENERATION_I;

            System.out.println("ExecutionServlet: Executing with architecture: " + architecture.name() + " (Cost: " + architecture.getCost() + " credits)");

            // Get user and validate credits
            ServerContext context = ServerContext.getInstance();
            User user = context.getUser(username);

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(GSON.toJson("User not found"));
                return;
            }

            // Check if user has enough credits
            if (!user.hasEnoughCredits(architecture.getCost())) {
                response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
                response.getWriter().write(GSON.toJson("Insufficient credits. Required: " + architecture.getCost() + ", Available: " + user.getCredits()));
                return;
            }

            // Validate that the program can run on the selected architecture
            String validationError = validateProgramForArchitecture(engine, architecture);
            if (validationError != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(GSON.toJson(validationError));
                return;
            }

            // Deduct credits BEFORE execution
            boolean creditDeducted = user.deductCredits(architecture.getCost());
            if (!creditDeducted) {
                response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
                response.getWriter().write(GSON.toJson("Failed to deduct credits"));
                return;
            }

            System.out.println("ExecutionServlet: Credits deducted successfully. User " + username + " now has " + user.getCredits() + " credits");

            // Execute the program with the selected architecture
            engine.executeProgram(engine.getCurrentDegree(), true);

            // Capture execution results for history tracking
            int finalYValue = getFinalYValue(engine);
            int cpuCyclesUsed = engine.getCycleSum();
            String executionType = determineExecutionType(currentTarget, engine);
            String architectureTypeStr = architecture.name().replace("GENERATION_", "");
            String executionLevel = "Run"; // This is a normal execution, not debug

            // Add detailed execution record to user's history
            user.addExecutionRecord(
                executionType,           // "Main Program" or "Helper Function"
                currentTarget,           // Program/function name
                architectureTypeStr,     // "I", "II", "III", "IV"
                executionLevel,          // "Run" or "Debug"
                finalYValue,             // Final value of variable y
                cpuCyclesUsed           // Total cycles consumed
            );

            System.out.println("ExecutionServlet: Program executed successfully with architecture: " + architecture.name());
            System.out.println("ExecutionServlet: User " + username + " total executions: " + user.getTotalExecutions());
            System.out.println("ExecutionServlet: Execution record added - Type: " + executionType +
                             ", Y-value: " + finalYValue + ", Cycles: " + cpuCyclesUsed);

            // Return updated execution state
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget);
            response.getWriter().write(GSON.toJson(executionState));

        } catch (Exception e) {
            System.err.println("ExecutionServlet: Error during execution: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON.toJson("Execution failed: " + e.getMessage()));
        }
    }

    /**
     * Validates that the program can run on the specified architecture by checking
     * if all commands are supported by the architecture generation.
     */
    private String validateProgramForArchitecture(S_Emulator engine, Architecture architecture) {
        try {
            // Get all commands from the current program
            List<Command> commands = engine.getCommands();
            if (commands == null || commands.isEmpty()) {
                return null; // No commands to validate
            }

            // Check each command against architecture capabilities
            for (Command command : commands) {
                if (!isCommandSupportedByArchitecture(command, architecture)) {
                    return "Program contains unsupported command '" + command.getClass().getSimpleName() +
                           "' for architecture " + architecture.name() + ". Please select a higher generation architecture.";
                }
            }

            return null; // All commands supported
        } catch (Exception e) {
            System.err.println("Error validating program for architecture: " + e.getMessage());
            return "Error validating program compatibility: " + e.getMessage();
        }
    }

    /**
     * Checks if a specific command is supported by the given architecture generation.
     * Each generation supports all commands from previous generations plus new ones.
     */
    private boolean isCommandSupportedByArchitecture(Command command, Architecture architecture) {
        String commandType = command.getClass().getSimpleName().toUpperCase();

        switch (architecture) {
            case GENERATION_I:
                // Only basic commands
                return commandType.equals("NEUTRAL") ||
                       commandType.equals("INCREASE") ||
                       commandType.equals("DECREASE") ||
                       commandType.equals("JUMPNOTZERO") ||
                       commandType.equals("JUMP_NOT_ZERO");

            case GENERATION_II:
                // Generation I + new synthetic commands
                return isCommandSupportedByArchitecture(command, Architecture.GENERATION_I) ||
                       commandType.equals("ZEROVARIABLE") ||
                       commandType.equals("ZERO_VARIABLE") ||
                       commandType.equals("CONSTANTASSIGNMENT") ||
                       commandType.equals("CONSTANT_ASSIGNMENT") ||
                       commandType.equals("GOTOLABEL") ||
                       commandType.equals("GOTO_LABEL");

            case GENERATION_III:
                // Generation II + new synthetic commands
                return isCommandSupportedByArchitecture(command, Architecture.GENERATION_II) ||
                       commandType.equals("ASSIGNMENT") ||
                       commandType.equals("JUMPZERO") ||
                       commandType.equals("JUMP_ZERO") ||
                       commandType.equals("JUMPEQUALCONSTANT") ||
                       commandType.equals("JUMP_EQUAL_CONSTANT") ||
                       commandType.equals("JUMPEQUALVARIABLE") ||
                       commandType.equals("JUMP_EQUAL_VARIABLE");

            case GENERATION_IV:
                // Generation III + new synthetic commands
                return isCommandSupportedByArchitecture(command, Architecture.GENERATION_III) ||
                       commandType.equals("QUOTE") ||
                       commandType.equals("JUMPEQUALFUNCTION") ||
                       commandType.equals("JUMP_EQUAL_FUNCTION");

            default:
                return false;
        }
    }

    private void handleSetDegree(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String functionName = request.getParameter("name");
        String valueStr = request.getParameter("value");
        String deltaStr = request.getParameter("delta");

        HttpSession session = request.getSession();
        S_Emulator engine = (S_Emulator) session.getAttribute("engine");
        String currentTarget = (String) session.getAttribute("currentTarget");

        if (engine == null || currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active execution session"));
            return;
        }

        try {
            int newDegree = engine.getCurrentDegree(); // Start with current degree

            if (valueStr != null && !valueStr.trim().isEmpty()) {
                // Direct degree setting
                newDegree = Integer.parseInt(valueStr);
                System.out.println("ExecutionServlet: Setting degree directly to: " + newDegree);
            } else if (deltaStr != null && !deltaStr.trim().isEmpty()) {
                // Delta change (expand/collapse)
                int delta = Integer.parseInt(deltaStr);
                newDegree = engine.getCurrentDegree() + delta;
                System.out.println("ExecutionServlet: Changing degree by delta " + delta + " from " + engine.getCurrentDegree() + " to " + newDegree);
            }

            // Ensure degree is within valid bounds
            int maxDegree = engine.getMaxExpansionDepth();
            if (newDegree < 0) {
                newDegree = 0;
            } else if (newDegree > maxDegree) {
                newDegree = maxDegree;
            }

            // Set the new degree in the engine
            engine.setCurrentDegree(newDegree);
            System.out.println("ExecutionServlet: Degree set to: " + newDegree);

            // Return updated execution state with instructions and variables at the new degree
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget);
            response.getWriter().write(GSON.toJson(executionState));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("Invalid degree value"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON.toJson("Failed to set degree: " + e.getMessage()));
        }
    }

    private void handleSelectFunction(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String functionName = request.getParameter("name");
        HttpSession session = request.getSession();

        // Store selected function in session
        session.setAttribute("selectedFunction", functionName);

        S_Emulator engine = (S_Emulator) session.getAttribute("engine");
        String currentTarget = (String) session.getAttribute("currentTarget");

        if (engine != null && currentTarget != null) {
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget);
            response.getWriter().write(GSON.toJson(executionState));
        } else {
            response.getWriter().write(GSON.toJson("{}"));
        }
    }

    private void handleNewRun(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        String currentTarget = (String) session.getAttribute("currentTarget");
        S_Emulator engine = (S_Emulator) session.getAttribute("engine");

        if (currentTarget == null || engine == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active program"));
            return;
        }

        try {
            System.out.println("ExecutionServlet: handleNewRun - resetting ALL variables to 0 and resetting execution state");

            // Exit debug mode if active
            Boolean debugMode = (Boolean) session.getAttribute("debugMode");
            if (debugMode != null && debugMode) {
                System.out.println("Exiting debug mode before new run");
                session.setAttribute("debugMode", false);
                if (engine instanceof Program) {
                    ((Program) engine).stopDebugging();
                }
            }

            // Reset ALL variables to 0 (input, output, and work variables)
            Set<Variable> variables = engine.getVariables();
            for (Variable variable : variables) {
                variable.setValue(0);
                System.out.println("Reset variable: " + variable.getName() + " to 0 (type: " + variable.getClass().getSimpleName() + ")");
            }

            // Reset execution state
            if (engine instanceof Program) {
                Program program = (Program) engine;
                program.setCycleSum(0);
                program.setCurrentCommand(null);
                System.out.println("Reset execution state (cycles and current command)");
            }

            // Log final state to confirm all variables are reset
            System.out.println("Final variable values after new run:");
            for (Variable var : engine.getVariables()) {
                System.out.println("  " + var.getName() + " = " + var.getValue() + " (type: " + var.getClass().getSimpleName() + ")");
            }

            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget);
            response.getWriter().write(GSON.toJson(executionState));

        } catch (Exception e) {
            System.err.println("ExecutionServlet: Error in handleNewRun: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON.toJson("Failed to create new run: " + e.getMessage()));
        }
    }

    private void handleDebug(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String operation = request.getParameter("op");
        HttpSession session = request.getSession();
        S_Emulator engine = (S_Emulator) session.getAttribute("engine");
        String currentTarget = (String) session.getAttribute("currentTarget");

        if (engine == null || currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active execution session"));
            return;
        }

        try {
            System.out.println("Debug operation: " + operation);

            // Handle debug operations
            switch (operation) {
                case "start":
                    // Start debugging mode - prepare the engine for step-by-step execution
                    engine.prepareForDebugging();
                    session.setAttribute("debugMode", true);
                    System.out.println("Debug mode started");
                    break;

                case "step":
                    // Step to next instruction
                    Boolean debugMode = (Boolean) session.getAttribute("debugMode");
                    if (debugMode == null || !debugMode) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write(GSON.toJson("Not in debug mode"));
                        return;
                    }
                    engine.stepOver();

                    // Increment execution count for debug step operations
                    String username = (String) session.getAttribute("username");
                    if (username != null) {
                        ServerContext context = ServerContext.getInstance();
                        User user = context.getUser(username);
                        if (user != null) {
                            user.incrementExecutionCount();
                            System.out.println("ExecutionServlet: Debug step - User " + username + " total executions: " + user.getTotalExecutions());
                        }
                    }

                    System.out.println("Stepped to next instruction");

                    // Check if program has completed after step - if so, exit debug mode
                    if (engine instanceof Program) {
                        Program program = (Program) engine;
                        if (!program.isInDebugMode()) {
                            session.setAttribute("debugMode", false);
                            program.stopDebugging();
                            System.out.println("Debug mode completed after step - exiting debug mode");
                        }
                    }
                    break;

                case "cont":
                    // Continue execution - run remaining program
                    debugMode = (Boolean) session.getAttribute("debugMode");
                    if (debugMode == null || !debugMode) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write(GSON.toJson("Not in debug mode"));
                        return;
                    }
                    // Continue by executing the rest of the program
                    engine.executeProgram(engine.getCurrentDegree(), true);

                    // Ensure debug mode is properly terminated after continuation
                    session.setAttribute("debugMode", false);
                    if (engine instanceof Program) {
                        Program program = (Program) engine;
                        program.stopDebugging();
                    }
                    System.out.println("Continued execution to completion - debug mode terminated");
                    break;

                case "stop":
                    // Stop debugging - reset to normal mode
                    session.setAttribute("debugMode", false);
                    if (engine instanceof Program) {
                        Program program = (Program) engine;
                        program.stopDebugging();
                    }
                    engine.reset(); // Reset to initial state
                    System.out.println("Debug mode stopped and program reset");
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(GSON.toJson("Unknown debug operation: " + operation));
                    return;
            }

            // Create updated execution state - this will handle debug state properly
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget);

            response.getWriter().write(GSON.toJson(executionState));

        } catch (Exception e) {
            System.err.println("Debug operation failed: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON.toJson("Debug operation failed: " + e.getMessage()));
        }
    }

    private void handleGetParentChain(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String commandIdStr = request.getParameter("commandId");
        HttpSession session = request.getSession();
        S_Emulator engine = (S_Emulator) session.getAttribute("engine");
        String currentTarget = (String) session.getAttribute("currentTarget");

        if (engine == null || currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active execution session"));
            return;
        }

        if (commandIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("Missing commandId parameter"));
            return;
        }

        try {
            int commandId = Integer.parseInt(commandIdStr);
            int currentDegree = engine.getCurrentDegree();

            // Get the parent command chain for the specified command
            List<String> parentChain = engine.getParentCommandChain(commandId, currentDegree);

            response.getWriter().write(GSON.toJson(parentChain));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("Invalid commandId format"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON.toJson("Failed to get parent chain: " + e.getMessage()));
        }
    }

    private void handleUpdateInput(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String inputName = request.getParameter("name");
        String inputValue = request.getParameter("value");

        System.out.println("ExecutionServlet: handleUpdateInput called with name: '" + inputName + "', value: '" + inputValue + "'");

        HttpSession session = request.getSession();
        S_Emulator engine = (S_Emulator) session.getAttribute("engine");
        String currentTarget = (String) session.getAttribute("currentTarget");

        if (engine == null || currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active execution session"));
            return;
        }

        if (inputName == null || inputValue == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("Missing name or value parameter"));
            return;
        }

        try {
            // Update the input variable in the engine (do NOT execute)
            engine.updateInputVariable(inputName, inputValue);
            System.out.println("ExecutionServlet: Successfully updated input variable " + inputName + " = " + inputValue);

            // Return simple success response WITHOUT calling createExecutionStateDTO
            // This prevents automatic execution during input updates
            response.getWriter().write(GSON.toJson("Input variable updated successfully"));

        } catch (Exception e) {
            System.err.println("ExecutionServlet: Failed to update input variable " + inputName + ": " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON.toJson("Input update failed: " + e.getMessage()));
        }
    }

    private S_Emulator getOrCreateEngine(HttpSession session, String target) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new Exception("User not logged in");
        }

        // Load the program from ServerContext
        S_Emulator engine = loadProgramFromContext(target, username);
        if (engine == null) {
            throw new Exception("Program not found: " + target);
        }

        // Store engine in session
        session.setAttribute("engine", engine);

        return engine;
    }

    private S_Emulator loadProgramFromContext(String programName, String currentUser) {
        ServerContext context = ServerContext.getInstance();

        // First try to find the program owned by the current user
        S_Emulator program = context.getUserProgram(currentUser, programName);
        if (program != null) {
            return program;
        }

        // If not found, search through all users' programs (since any user can access any program)
        Map<String, S_Emulator> allPrograms = context.getAllStoredPrograms();
        for (Map.Entry<String, S_Emulator> entry : allPrograms.entrySet()) {
            String compositeKey = entry.getKey(); // userId_programName format
            S_Emulator emulator = entry.getValue();

            // Extract program name from composite key
            String[] keyParts = compositeKey.split("_", 2);
            String storedProgramName = keyParts.length > 1 ? keyParts[1] : "";

            // Also check the actual program name from the emulator
            String actualProgramName = emulator.getCurrentProgramName();

            if (programName.equals(storedProgramName) || programName.equals(actualProgramName)) {
                return emulator;
            }

            // NEW: Search for functions within this program
            if (emulator instanceof Program) {
                Program mainProgram = (Program) emulator;
                // Check if the target is a function within this program
                for (Program subFunction : mainProgram.subFunctions) {
                    if (programName.equals(subFunction.getCurrentProgramName()) ||
                        programName.equals(subFunction.getUserString())) {
                        // Return the sub-function as the target for execution
                        return subFunction;
                    }
                }
            }
        }

        return null; // Program not found
    }

    private void setEngineCurrentDegree(S_Emulator engine, int targetDegree) {
        int currentDegree = engine.getCurrentDegree();

        if (targetDegree > currentDegree) {
            // Need to increase degree
            for (int i = currentDegree; i < targetDegree; i++) {
                engine.increaseDegree();
            }
        } else if (targetDegree < currentDegree) {
            // Need to decrease degree
            for (int i = currentDegree; i > targetDegree; i--) {
                engine.decreaseDegree();
            }
        }
        // If targetDegree == currentDegree, no change needed
    }

    private Program loadProgram(String programName) {
        // This method is now deprecated - use loadProgramFromContext instead
        return null;
    }

    private ExecutionStateDTO createExecutionStateDTO(S_Emulator engine, String target) {
        ExecutionStateDTO state = new ExecutionStateDTO();

        // Set basic information
        state.setSelectedFunction(target);
        state.setCurrentDegree(engine.getCurrentDegree()); // Use actual current degree from engine
        state.setMaxDegree(engine.getMaxExpansionDepth()); // Use actual max degree from engine
        state.setCycles(engine.getCycleSum()); // Use getCycleSum() method from S_Emulator

        // Set debug state properly
        Boolean debugMode = false;
        if (engine instanceof Program) {
            Program program = (Program) engine;
            debugMode = program.isInDebugMode();
        }
        state.setDebugging(debugMode);

        // Set function names (for now just the target)
        List<String> functionNames = new ArrayList<>();
        functionNames.add(target);
        state.setFunctionNames(functionNames);

        // Get instructions from the engine using proper methods
        List<InstructionDTO> instructions = getInstructionsFromEngine(engine);
        state.setInstructions(instructions);

        // Set highlighted instruction for debug mode
        if (debugMode) {
            Command currentDebugCommand = engine.getCurrentDebugCommand();
            if (currentDebugCommand != null) {
                // Find the instruction ID that matches the current debug command
                for (InstructionDTO instruction : instructions) {
                    if (instruction.getId() == currentDebugCommand.getId()) {
                        state.setHighlightedInstructionId(currentDebugCommand.getId());
                        System.out.println("Highlighted debug instruction ID: " + currentDebugCommand.getId());
                        break;
                    }
                }
            }
        }

        // Get variables using proper methods
        List<VariableDTO> allVariables = getVariablesFromEngine(engine);
        List<VariableDTO> inputVariables = getInputVariablesFromEngine(engine);
        state.setAllVariables(allVariables);
        state.setInputVariables(inputVariables);

        // Get changed variables from the program if in debug mode
        Set<String> changedVariables = new HashSet<>();
        if (debugMode && engine instanceof Program) {
            Program program = (Program) engine;
            changedVariables = program.getChangedVariableNames();
            System.out.println("Changed variables in debug mode: " + changedVariables);
        }
        state.setChangedVariableNames(changedVariables);

        // Set empty trace lines for now
        state.setTraceLines(new ArrayList<>());

        return state;
    }

    private List<InstructionDTO> getInstructionsFromEngine(S_Emulator engine) {
        List<InstructionDTO> instructions = new ArrayList<>();

        // Use the correct method from S_Emulator interface to get commands at current degree
        List<Command> commands = engine.getCommandsAtDesiredLevel(engine.getCurrentDegree());
        System.out.println("ExecutionServlet: Retrieved " + commands.size() + " commands from engine");

        for (int i = 0; i < commands.size(); i++) {
            Command command = commands.get(i);
            InstructionDTO instruction = new InstructionDTO();
            // FIX: Use the command's actual ID (which is 1-based) instead of the loop index
            instruction.setId(command.getId());
            instruction.setInstruction(command.toString());

            // Get associated variables if they exist
            Variable[] assocVars = command.getAssociatedVariables();
            String arguments = "";
            if (assocVars != null && assocVars.length > 0) {
                arguments = Arrays.stream(assocVars)
                    .filter(Objects::nonNull)
                    .map(Variable::getName)
                    .collect(Collectors.joining(", "));
            }
            instruction.setArguments(arguments);

            // Set the additional fields expected by InstructionTableController
            instruction.setType(command.getType()); // e.g., "Increase", "Decrease", etc.
            instruction.setCycles(command.getCycles()); // Use actual cycles from command
            instruction.setLabel(command.getLabel()); // Use actual label from command
            instruction.setText(command.toString()); // Display text for the instruction

            instructions.add(instruction);
            System.out.println("ExecutionServlet: Added instruction " + command.getId() + ": " + command.toString());
        }

        return instructions;
    }

    private List<VariableDTO> getVariablesFromEngine(S_Emulator engine) {
        List<VariableDTO> variables = new ArrayList<>();
        Set<String> relevantVariableNames = new HashSet<>();

        // Get commands at the current degree level
        List<Command> commands = engine.getCommandsAtDesiredLevel(engine.getCurrentDegree());

        // Collect all variable names used in the current degree's commands
        for (Command command : commands) {
            Variable[] assocVars = command.getAssociatedVariables();
            if (assocVars != null) {
                for (Variable var : assocVars) {
                    if (var != null) {
                        relevantVariableNames.add(var.getName());
                    }
                }
            }
        }

        // Get all variables from engine
        Set<Variable> allVariableSet = engine.getVariables();
        System.out.println("ExecutionServlet: Retrieved " + allVariableSet.size() + " total variables from engine");
        System.out.println("ExecutionServlet: Filtering to show only variables used in current degree commands: " + relevantVariableNames);

        // Only include variables that are used in the current degree's commands
        for (Variable variable : allVariableSet) {
            if (relevantVariableNames.contains(variable.getName()) && !(variable instanceof InputVariable)) {
                VariableDTO varDTO = new VariableDTO();
                varDTO.setName(variable.getName());
                varDTO.setValue(variable.getValue());
                varDTO.setType(variable.getClass().getSimpleName());
                variables.add(varDTO);
                System.out.println("ExecutionServlet: Added relevant variable: " + variable.getName() + " = " + variable.getValue());
            }
        }

        System.out.println("ExecutionServlet: Filtered variables list contains " + variables.size() + " variables for current degree");
        return variables;
    }

    private List<VariableDTO> getInputVariablesFromEngine(S_Emulator engine) {
        List<VariableDTO> inputVariables = new ArrayList<>();

        // Use the correct method from S_Emulator interface to get variables
        Set<Variable> variableSet = engine.getVariables();

        for (Variable variable : variableSet) {
            if (variable instanceof InputVariable) {
                VariableDTO varDTO = new VariableDTO();
                varDTO.setName(variable.getName());
                varDTO.setValue(variable.getValue());
                varDTO.setType("InputVariable");
                inputVariables.add(varDTO);
            }
        }

        return inputVariables;
    }

    /**
     * Get the final value of variable 'y' after execution
     */
    private int getFinalYValue(S_Emulator engine) {
        try {
            Set<Variable> variables = engine.getVariables();
            for (Variable variable : variables) {
                if ("y".equalsIgnoreCase(variable.getName()) || "Y".equals(variable.getName())) {
                    return variable.getValue();
                }
            }
            // If no 'y' variable found, check for output variable
            for (Variable variable : variables) {
                if (variable instanceof OutputVariable) {
                    return variable.getValue();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting final Y value: " + e.getMessage());
        }
        return 0; // Default if no Y variable found
    }

    /**
     * Determine if the execution target is a main program or helper function
     */
    private String determineExecutionType(String targetName, S_Emulator engine) {
        try {
            ServerContext context = ServerContext.getInstance();
            Map<String, S_Emulator> allPrograms = context.getAllStoredPrograms();

            // Check if this target is a main program (exists as a key in stored programs)
            for (Map.Entry<String, S_Emulator> entry : allPrograms.entrySet()) {
                String compositeKey = entry.getKey(); // userId_programName format
                String[] keyParts = compositeKey.split("_", 2);
                String storedProgramName = keyParts.length > 1 ? keyParts[1] : "";

                if (targetName.equals(storedProgramName)) {
                    return "Main Program";
                }

                // Also check the actual program name
                S_Emulator emulator = entry.getValue();
                if (emulator instanceof Program) {
                    Program program = (Program) emulator;
                    if (targetName.equals(program.getCurrentProgramName())) {
                        return "Main Program";
                    }
                }
            }

            // If not found as main program, it's likely a helper function
            return "Helper Function";

        } catch (Exception e) {
            System.err.println("Error determining execution type: " + e.getMessage());
            return "Unknown";
        }
    }
}
