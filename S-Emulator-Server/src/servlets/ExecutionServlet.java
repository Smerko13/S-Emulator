package servlets;

import api.dto.*;
import api.util.ArchitectureValidator;
import com.google.gson.Gson;
import engine.S_Emulator;
import engine.Program;
import engine.commands.Command;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;

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

        if (target == null || target.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("Missing target parameter"));
            return;
        }

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(GSON.toJson("User not logged in"));
            return;
        }

        try {
            // Create or get the engine for this user/program combination
            S_Emulator engine = getOrCreateEngine(session, target);

            // Create execution state DTO
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, target);

            // Store current program/function in session
            session.setAttribute("currentTarget", target);

            response.getWriter().write(GSON.toJson(executionState));

        } catch (Exception e) {
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
                    execRequest = GSON.fromJson(requestBody.toString(), ExecuteProgramRequest.class);
                }
            }

            // Default to Generation I if no architecture specified
            Architecture architecture = (execRequest != null && execRequest.architecture != null)
                ? execRequest.architecture
                : Architecture.GENERATION_I;


            // Get user
            ServerContext context = ServerContext.getInstance();
            User user = context.getUser(username);

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(GSON.toJson("User not found"));
                return;
            }

            // Validate that the program can run on the selected architecture
            ArchitectureValidationDTO validation = validateProgramForArchitectureDTO(engine, architecture);
            if (!validation.isValid()) {
                // Return structured validation error with incompatible instruction IDs
                ExecuteProgramResponse errorResponse = new ExecuteProgramResponse(validation);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(GSON.toJson(errorResponse));
                return;
            }

            // Capture ORIGINAL input values BEFORE execution (for Re-Run functionality)
            List<VariableDTO> originalInputs = new ArrayList<>();
            Set<Variable> variablesBeforeExecution = engine.getVariables();
            for (Variable var : variablesBeforeExecution) {
                if (var instanceof InputVariable) {
                    VariableDTO inputDTO = new VariableDTO(var.getName(), var.getValue(), "Input");
                    inputDTO.setInput(true);
                    originalInputs.add(inputDTO);
                }
            }

            // Execute the program with the selected architecture
            engine.executeProgram(engine.getCurrentDegree(), true);

            // Calculate total cost = architecture cost + cycleSum
            int cpuCyclesUsed = engine.getCycleSum();
            int totalCost = architecture.getCost() + cpuCyclesUsed;

            // Check if user has enough credits for the total cost
            if (!user.hasEnoughCredits(totalCost)) {
                response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
                response.getWriter().write(GSON.toJson("Insufficient credits. Required: " + totalCost + " (Architecture: " + architecture.getCost() + " + Cycles: " + cpuCyclesUsed + "), Available: " + user.getCredits()));
                return;
            }

            // Deduct credits AFTER execution based on architecture cost + cycles
            boolean creditDeducted = user.deductCredits(totalCost);
            if (!creditDeducted) {
                response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
                response.getWriter().write(GSON.toJson("Failed to deduct credits"));
                return;
            }


            // Record execution in global statistics for this program
            context.recordProgramExecution(currentTarget, totalCost);

            // Capture execution results for history tracking
            int finalYValue = getFinalYValue(engine);
            String executionType = determineExecutionType(currentTarget, engine);
            String architectureTypeStr = architecture.name().replace("GENERATION_", "");
            String executionLevel = "Run"; // This is a normal execution, not debug
            int expansionDegree = engine.getCurrentDegree(); // Get the degree at which it was executed

            // Add detailed execution record to user's history
            user.addExecutionRecord(
                executionType,           // "Main Program" or "Helper Function"
                currentTarget,           // Program/function name
                architectureTypeStr,     // "I", "II", "III", "IV"
                executionLevel,          // "Run" or "Debug"
                expansionDegree,         // Degree (0 to maxDepth)
                finalYValue,             // Final value of variable y
                cpuCyclesUsed           // Total cycles consumed
            );

            // Capture and store detailed execution data with all final variables
            List<VariableDTO> finalVariables = new ArrayList<>();
            Set<Variable> variables = engine.getVariables();
            for (Variable var : variables) {
                String varType = var instanceof InputVariable ? "Input" :
                                var instanceof OutputVariable ? "Output" : "Work";
                VariableDTO varDTO = new VariableDTO(var.getName(), var.getValue(), varType);
                finalVariables.add(varDTO);
            }

            ExecutionDetailsDTO detailsDTO = new ExecutionDetailsDTO(
                user.getTotalExecutions(),  // runId matches the execution number
                executionType,
                currentTarget,
                architectureTypeStr,
                executionLevel,
                cpuCyclesUsed,
                finalVariables,
                originalInputs  // NOW includes original input values from before execution
            );
            user.addExecutionDetails(detailsDTO);


            // Return updated execution state WITH user credits
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget, username);
            response.getWriter().write(GSON.toJson(executionState));

        } catch (Exception e) {
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
            return "Error validating program compatibility: " + e.getMessage();
        }
    }

    /**
     * NEW: DTO-based validation that returns structured error with incompatible instruction IDs
     */
    private ArchitectureValidationDTO validateProgramForArchitectureDTO(S_Emulator engine, Architecture architecture) {
        try {
            // Get instructions with required architecture set
            List<InstructionDTO> instructions = getInstructionsFromEngine(engine);

            // Use the ArchitectureValidator to validate
            ArchitectureValidationDTO validation = ArchitectureValidator.validate(instructions, architecture);


            return validation;

        } catch (Exception e) {
            // Return error validation result
            return new ArchitectureValidationDTO(
                false,
                "Error validating program compatibility: " + e.getMessage(),
                new ArrayList<>(),
                Architecture.GENERATION_I,
                architecture
            );
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
            } else if (deltaStr != null && !deltaStr.trim().isEmpty()) {
                // Delta change (expand/collapse)
                int delta = Integer.parseInt(deltaStr);
                newDegree = engine.getCurrentDegree() + delta;
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
        String username = (String) session.getAttribute("username");

        if (engine != null && currentTarget != null) {
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget, username);
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

            // Exit debug mode if active
            Boolean debugMode = (Boolean) session.getAttribute("debugMode");
            if (debugMode != null && debugMode) {
                session.setAttribute("debugMode", false);
                if (engine instanceof Program) {
                    ((Program) engine).stopDebugging();
                }
            }

            // Reset ALL variables to 0 (input, output, and work variables)
            Set<Variable> variables = engine.getVariables();
            for (Variable variable : variables) {
                variable.setValue(0);
            }

            // Reset execution state
            if (engine instanceof Program) {
                Program program = (Program) engine;
                program.setCycleSum(0);
                program.setCurrentCommand(null);
            }

            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget);
            response.getWriter().write(GSON.toJson(executionState));

        } catch (Exception e) {
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
        String username = (String) session.getAttribute("username");

        if (engine == null || currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active execution session"));
            return;
        }

        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(GSON.toJson("User not logged in"));
            return;
        }

        try {
            ServerContext context = ServerContext.getInstance();
            User user = context.getUser(username);

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(GSON.toJson("User not found"));
                return;
            }

            // Handle debug operations
            switch (operation) {
                case "start":
                    // Parse architecture from request body
                    Architecture architecture = Architecture.GENERATION_I; // Default
                    if ("POST".equalsIgnoreCase(request.getMethod())) {
                        StringBuilder requestBody = new StringBuilder();
                        String line;
                        while ((line = request.getReader().readLine()) != null) {
                            requestBody.append(line);
                        }
                        if (requestBody.length() > 0) {
                            ExecuteProgramRequest execRequest = GSON.fromJson(requestBody.toString(), ExecuteProgramRequest.class);
                            if (execRequest != null && execRequest.architecture != null) {
                                architecture = execRequest.architecture;
                            }
                        }
                    }

                    // Validate architecture compatibility
                    ArchitectureValidationDTO validation = validateProgramForArchitectureDTO(engine, architecture);
                    if (!validation.isValid()) {
                        ExecuteProgramResponse errorResponse = new ExecuteProgramResponse(validation);
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write(GSON.toJson(errorResponse));
                        return;
                    }

                    // Store architecture for this debug session
                    session.setAttribute("debugArchitecture", architecture);
                    session.setAttribute("debugPreviousCycles", 0); // Track cycles from previous steps
                    session.setAttribute("debugArchitectureCharged", false); // Track if architecture cost has been charged

                    // Start debugging mode - prepare the engine for step-by-step execution
                    engine.prepareForDebugging();
                    session.setAttribute("debugMode", true);
                    break;

                case "step":
                    // Step to next instruction
                    Boolean debugMode = (Boolean) session.getAttribute("debugMode");
                    if (debugMode == null || !debugMode) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write(GSON.toJson("Not in debug mode"));
                        return;
                    }

                    // Get debug architecture and previous cycles
                    Architecture debugArch = (Architecture) session.getAttribute("debugArchitecture");
                    if (debugArch == null) {
                        debugArch = Architecture.GENERATION_I; // Fallback
                    }
                    Integer previousCycles = (Integer) session.getAttribute("debugPreviousCycles");
                    if (previousCycles == null) {
                        previousCycles = 0;
                    }
                    Boolean architectureCharged = (Boolean) session.getAttribute("debugArchitectureCharged");
                    if (architectureCharged == null) {
                        architectureCharged = false;
                    }

                    // Execute the step
                    engine.stepOver();

                    // Calculate cost for this step
                    int currentCycles = engine.getCycleSum();
                    int newCycles = currentCycles - previousCycles;
                    int stepCost = debugArch.getCost() + currentCycles; // Total cost so far

                    // Check if user has enough credits
                    if (!user.hasEnoughCredits(stepCost)) {
                        response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
                        response.getWriter().write(GSON.toJson("Insufficient credits. Required: " + stepCost + " (Architecture: " + debugArch.getCost() + " + Cycles: " + currentCycles + "), Available: " + user.getCredits()));

                        // Stop debugging due to insufficient credits
                        session.setAttribute("debugMode", false);
                        if (engine instanceof Program) {
                            ((Program) engine).stopDebugging();
                        }
                        return;
                    }

                    // Deduct incremental cost: architecture cost (if not yet charged) + new cycles
                    int incrementalCost = architectureCharged ? newCycles : (debugArch.getCost() + newCycles);
                    user.deductCredits(incrementalCost);

                    // Mark architecture as charged after first step
                    if (!architectureCharged) {
                        session.setAttribute("debugArchitectureCharged", true);
                    }

                    // Update previous cycles for next step
                    session.setAttribute("debugPreviousCycles", currentCycles);

                    // Increment execution count for debug step operations
                    user.incrementExecutionCount();

                    // Check if program has completed after step - if so, exit debug mode
                    if (engine instanceof Program) {
                        Program program = (Program) engine;
                        if (!program.isInDebugMode()) {
                            // Program completed - calculate total cost for global tracking
                            int totalDebugCost = debugArch.getCost() + currentCycles;

                            // Record execution in global statistics
                            context.recordProgramExecution(currentTarget, totalDebugCost);

                            // Program completed - record execution history
                            int finalYValue = getFinalYValue(engine);
                            String executionType = determineExecutionType(currentTarget, engine);
                            String architectureTypeStr = debugArch.name().replace("GENERATION_", "");

                            user.addExecutionRecord(
                                executionType,
                                currentTarget,
                                architectureTypeStr,
                                "Debug",
                                program.getCurrentDegree(),
                                finalYValue,
                                currentCycles
                            );

                            session.setAttribute("debugMode", false);
                            program.stopDebugging();
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

                    // Get debug architecture and previous cycles
                    debugArch = (Architecture) session.getAttribute("debugArchitecture");
                    if (debugArch == null) {
                        debugArch = Architecture.GENERATION_I;
                    }
                    previousCycles = (Integer) session.getAttribute("debugPreviousCycles");
                    if (previousCycles == null) {
                        previousCycles = 0;
                    }
                    architectureCharged = (Boolean) session.getAttribute("debugArchitectureCharged");
                    if (architectureCharged == null) {
                        architectureCharged = false;
                    }

                    // Continue by executing the rest of the program
                    engine.executeProgram(engine.getCurrentDegree(), true);

                    // Calculate final cost
                    int finalCycles = engine.getCycleSum();
                    int remainingCycles = finalCycles - previousCycles;

                    // Total cost includes architecture (if not charged yet) + all cycles
                    int totalCostNeeded = debugArch.getCost() + finalCycles;

                    // Check if user has enough credits for completion
                    if (!user.hasEnoughCredits(totalCostNeeded)) {
                        response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
                        response.getWriter().write(GSON.toJson("Insufficient credits to complete. Required: " + totalCostNeeded + " (Architecture: " + debugArch.getCost() + " + Cycles: " + finalCycles + "), Available: " + user.getCredits()));
                        return;
                    }

                    // Deduct remaining cost: architecture (if not charged) + remaining cycles
                    int remainingCost = architectureCharged ? remainingCycles : (debugArch.getCost() + remainingCycles);
                    user.deductCredits(remainingCost);

                    if (!architectureCharged) {
                        session.setAttribute("debugArchitectureCharged", true);
                    }

                    // Record execution in global statistics
                    context.recordProgramExecution(currentTarget, totalCostNeeded);

                    // Record execution history
                    int finalYValue = getFinalYValue(engine);
                    String executionType = determineExecutionType(currentTarget, engine);
                    String architectureTypeStr = debugArch.name().replace("GENERATION_", "");
                    int expansionDegree = engine.getCurrentDegree(); // Get the degree

                    user.addExecutionRecord(
                        executionType,
                        currentTarget,
                        architectureTypeStr,
                        "Debug",
                        expansionDegree,
                        finalYValue,
                        finalCycles
                    );

                    // Capture and store detailed execution data
                    List<VariableDTO> finalVariables = new ArrayList<>();
                    Set<Variable> variables = engine.getVariables();
                    for (Variable var : variables) {
                        String varType = var instanceof InputVariable ? "Input" :
                                        var instanceof OutputVariable ? "Output" : "Work";
                        VariableDTO varDTO = new VariableDTO(var.getName(), var.getValue(), varType);
                        finalVariables.add(varDTO);
                    }

                    ExecutionDetailsDTO detailsDTO = new ExecutionDetailsDTO(
                        user.getTotalExecutions(),
                        executionType,
                        currentTarget,
                        architectureTypeStr,
                        "Debug",
                        finalCycles,
                        finalVariables,
                        new ArrayList<>()
                    );
                    user.addExecutionDetails(detailsDTO);

                    // Ensure debug mode is properly terminated after continuation
                    session.setAttribute("debugMode", false);
                    if (engine instanceof Program) {
                        Program program = (Program) engine;
                        program.stopDebugging();
                    }
                    break;

                case "stop":
                    // Stop debugging - reset to normal mode (no credit deduction)
                    session.setAttribute("debugMode", false);
                    session.removeAttribute("debugArchitecture");
                    session.removeAttribute("debugPreviousCycles");
                    session.removeAttribute("debugArchitectureCharged");
                    if (engine instanceof Program) {
                        Program program = (Program) engine;
                        program.stopDebugging();
                    }
                    engine.reset(); // Reset to initial state
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(GSON.toJson("Unknown debug operation: " + operation));
                    return;
            }

            // Create updated execution state - this will handle debug state properly
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget, username);

            response.getWriter().write(GSON.toJson(executionState));

        } catch (Exception e) {
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
            List<HistoryChainDTO> parentChain = engine.getParentCommandChainDTO(commandId, currentDegree);

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

            // Return simple success response WITHOUT calling createExecutionStateDTO
            // This prevents automatic execution during input updates
            response.getWriter().write(GSON.toJson("Input variable updated successfully"));

        } catch (Exception e) {
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
        }
        state.setChangedVariableNames(changedVariables);

        // Set empty trace lines for now
        state.setTraceLines(new ArrayList<>());

        return state;
    }

    /**
     * Overload that accepts username to include user credits in the state
     */
    private ExecutionStateDTO createExecutionStateDTO(S_Emulator engine, String target, String username) {
        ExecutionStateDTO state = createExecutionStateDTO(engine, target);

        // Add user's current credit balance
        if (username != null) {
            ServerContext context = ServerContext.getInstance();
            User user = context.getUser(username);
            if (user != null) {
                state.setUserCredits(user.getCredits());
            } else {
                state.setUserCredits(0);
            }
        } else {
            state.setUserCredits(0);
        }

        return state;
    }

    private List<InstructionDTO> getInstructionsFromEngine(S_Emulator engine) {
        List<InstructionDTO> instructions = new ArrayList<>();

        // Use the correct method from S_Emulator interface to get commands at current degree
        List<Command> commands = engine.getCommandsAtDesiredLevel(engine.getCurrentDegree());

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

            // *** NEW: Set the required architecture for this command ***
            Architecture requiredArch = getRequiredArchitectureForCommand(command);
            instruction.setRequiredArchitecture(requiredArch);
            instructions.add(instruction);
        }

        return instructions;
    }

    /**
     * Determine the required architecture for a command based on its type.
     * IMPORTANT: QUOTE commands (function calls) require Generation IV!
     */
    private Architecture getRequiredArchitectureForCommand(Command command) {
        String commandType = command.getClass().getSimpleName().toUpperCase();

        // *** GENERATION IV - QUOTE and Function Calls (CHECK FIRST!) ***
        // Programs like "divide 2" use QUOTE to call helper functions
        if (commandType.equals("QUOTE") ||
            commandType.equals("JUMPEQUALFUNCTION") ||
            commandType.equals("JUMP_EQUAL_FUNCTION")) {
            return Architecture.GENERATION_IV;
        }

        // Generation III - Advanced synthetic commands
        if (commandType.equals("ASSIGNMENT") ||
            commandType.equals("JUMPZERO") ||
            commandType.equals("JUMP_ZERO") ||
            commandType.equals("JUMPEQUALCONSTANT") ||
            commandType.equals("JUMP_EQUAL_CONSTANT") ||
            commandType.equals("JUMPEQUALVARIABLE") ||
            commandType.equals("JUMP_EQUAL_VARIABLE")) {
            return Architecture.GENERATION_III;
        }

        // Generation II - Synthetic commands (first tier)
        if (commandType.equals("ZEROVARIABLE") ||
            commandType.equals("ZERO_VARIABLE") ||
            commandType.equals("CONSTANTASSIGNMENT") ||
            commandType.equals("CONSTANT_ASSIGNMENT") ||
            commandType.equals("GOTOLABEL") ||
            commandType.equals("GOTO_LABEL")) {
            return Architecture.GENERATION_II;
        }

        // Generation I - Basic commands only
        if (commandType.equals("NEUTRAL") ||
            commandType.equals("INCREASE") ||
            commandType.equals("DECREASE") ||
            commandType.equals("JUMPNOTZERO") ||
            commandType.equals("JUMP_NOT_ZERO")) {
            return Architecture.GENERATION_I;
        }

        // Default to Generation I if unknown
        return Architecture.GENERATION_I;
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

        // Only include variables that are used in the current degree's commands
        for (Variable variable : allVariableSet) {
            if (relevantVariableNames.contains(variable.getName()) && !(variable instanceof InputVariable)) {
                VariableDTO varDTO = new VariableDTO();
                varDTO.setName(variable.getName());
                varDTO.setValue(variable.getValue());
                varDTO.setType(variable.getClass().getSimpleName());
                variables.add(varDTO);
            }
        }

        return variables;
    }

    private List<VariableDTO> getInputVariablesFromEngine(S_Emulator engine) {
        List<VariableDTO> inputVariables = new ArrayList<>();

        // Use the correct method from S_Emulator interface to get variables
        Set<Variable> variableSet = engine.getVariables();

        // Collect variables that are actually used at degree 0 (top-level only)
        Set<String> topLevelInputVariables = new HashSet<>();
        List<Command> topLevelCommands = engine.getCommandsAtDesiredLevel(0);

        for (Command command : topLevelCommands) {
            Variable[] assocVars = command.getAssociatedVariables();
            if (assocVars != null) {
                for (Variable var : assocVars) {
                    if (var != null && var instanceof InputVariable) {
                        topLevelInputVariables.add(var.getName());
                    }
                }
            }
        }


        // Only include InputVariables that are actually used at the top level
        for (Variable variable : variableSet) {
            if (variable instanceof InputVariable && topLevelInputVariables.contains(variable.getName())) {
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
            return "Unknown";
        }
    }
}
