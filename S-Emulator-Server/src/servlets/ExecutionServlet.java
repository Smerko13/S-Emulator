package servlets;

import api.dto.*;
import com.google.gson.Gson;
import engine.S_Emulator;
import engine.Program;
import engine.commands.Command;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;

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
        S_Emulator engine = (S_Emulator) session.getAttribute("engine");
        String currentTarget = (String) session.getAttribute("currentTarget");

        if (engine == null || currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active execution session"));
            return;
        }

        try {
            // Execute the program - use the correct method from S_Emulator interface
            engine.executeProgram(engine.getCurrentDegree(), true);

            // Return updated execution state
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget);
            response.getWriter().write(GSON.toJson(executionState));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON.toJson("Execution failed: " + e.getMessage()));
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

        if (currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active program"));
            return;
        }

        try {
            // Create new engine instance for fresh run
            S_Emulator engine = getOrCreateEngine(session, currentTarget);

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

        if (engine == null || currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active execution session"));
            return;
        }

        try {
            // Handle debug operations
            switch (operation) {
                case "start":
                    // Start debugging mode
                    break;
                case "step":
                    // Step to next instruction
                    break;
                case "cont":
                    // Continue execution
                    break;
                case "stop":
                    // Stop debugging
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(GSON.toJson("Unknown debug operation"));
                    return;
            }

            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget);
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

        HttpSession session = request.getSession();
        S_Emulator engine = (S_Emulator) session.getAttribute("engine");
        String currentTarget = (String) session.getAttribute("currentTarget");

        if (engine == null || currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active execution session"));
            return;
        }

        try {
            // Update the input variable in the engine
            engine.updateInputVariable(inputName, inputValue);

            // Return updated execution state
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget);
            response.getWriter().write(GSON.toJson(executionState));

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
        state.setDebugging(false); // Default or get from engine state

        // Set function names (for now just the target)
        List<String> functionNames = new ArrayList<>();
        functionNames.add(target);
        state.setFunctionNames(functionNames);

        // Get instructions from the engine using proper methods
        List<InstructionDTO> instructions = getInstructionsFromEngine(engine);
        state.setInstructions(instructions);

        // Get variables using proper methods
        List<VariableDTO> allVariables = getVariablesFromEngine(engine);
        List<VariableDTO> inputVariables = getInputVariablesFromEngine(engine);
        state.setAllVariables(allVariables);
        state.setInputVariables(inputVariables);

        // Set empty changed variables for now
        state.setChangedVariableNames(new HashSet<>());

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
            instruction.setType(command.getClass().getSimpleName()); // e.g., "Increase", "Decrease", etc.
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
            if (relevantVariableNames.contains(variable.getName())) {
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
}
