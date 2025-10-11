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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

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
        S_Emulator engine = (S_Emulator) session.getAttribute("engine");
        String currentTarget = (String) session.getAttribute("currentTarget");

        if (engine == null || currentTarget == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON.toJson("No active execution session"));
            return;
        }

        try {
            // Execute the program
            engine.runProgram();

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
            // Handle degree change logic here
            // This would depend on your engine's degree management implementation

            // Return updated execution state
            ExecutionStateDTO executionState = createExecutionStateDTO(engine, currentTarget);
            response.getWriter().write(GSON.toJson(executionState));

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
        }

        return null; // Program not found
    }

    private Program loadProgram(String programName) {
        // This method is now deprecated - use loadProgramFromContext instead
        return null;
    }

    private ExecutionStateDTO createExecutionStateDTO(S_Emulator engine, String target) {
        ExecutionStateDTO state = new ExecutionStateDTO();

        // Set basic information
        state.setSelectedFunction(target);
        state.setCurrentDegree(0); // Default or get from engine
        state.setMaxDegree(10);    // Default or get from program
        state.setCycles(engine.getStats() != null ? engine.getStats().getCycles() : 0);
        state.setDebugging(false); // Default or get from engine state

        // Set function names (for now just the target)
        List<String> functionNames = new ArrayList<>();
        functionNames.add(target);
        state.setFunctionNames(functionNames);

        // Get instructions from the engine/program
        List<InstructionDTO> instructions = getInstructionsFromEngine(engine);
        state.setInstructions(instructions);

        // Get variables
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

        if (engine.getProgram() != null && engine.getProgram().getCommands() != null) {
            List<Command> commands = engine.getProgram().getCommands();

            for (int i = 0; i < commands.size(); i++) {
                Command command = commands.get(i);
                InstructionDTO instruction = new InstructionDTO();
                instruction.setId(i);
                instruction.setInstruction(command.toString());
                instruction.setArguments(command.getArguments() != null ? command.getArguments().toString() : "");

                // Set the additional fields expected by InstructionTableController
                instruction.setType(command.getClass().getSimpleName()); // e.g., "Increase", "Decrease", etc.
                instruction.setCycles(1); // Default to 1 cycle per instruction
                instruction.setLabel(null); // Most instructions won't have labels
                instruction.setText(command.toString()); // Display text for the instruction

                instructions.add(instruction);
            }
        }

        return instructions;
    }

    private List<VariableDTO> getVariablesFromEngine(S_Emulator engine) {
        List<VariableDTO> variables = new ArrayList<>();

        if (engine.getProgram() != null && engine.getProgram().getVariables() != null) {
            for (Variable variable : engine.getProgram().getVariables()) {
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

        if (engine.getProgram() != null && engine.getProgram().getVariables() != null) {
            for (Variable variable : engine.getProgram().getVariables()) {
                if (variable instanceof InputVariable) {
                    VariableDTO varDTO = new VariableDTO();
                    varDTO.setName(variable.getName());
                    varDTO.setValue(variable.getValue());
                    varDTO.setType("InputVariable");
                    inputVariables.add(varDTO);
                }
            }
        }

        return inputVariables;
    }
}
