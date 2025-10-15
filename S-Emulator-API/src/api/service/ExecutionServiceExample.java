package api.service;

import api.dto.*;
import api.util.ArchitectureValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REFERENCE IMPLEMENTATION - Example service showing how to validate architecture before execution.
 *
 * This class demonstrates the complete flow:
 * 1. When a program is loaded, set requiredArchitecture for each instruction based on the command type
 * 2. Before execution, validate that user's architecture is sufficient
 * 3. Return validation errors if architecture is too low
 * 4. Only execute if validation passes
 */
public class ExecutionServiceExample {

    // Map of commands to their required architecture levels
    private static final Map<String, Architecture> COMMAND_ARCHITECTURE_MAP = new HashMap<>();

    static {
        // Generation I - Basic operations (default for most simple commands)
        COMMAND_ARCHITECTURE_MAP.put("MOV", Architecture.GENERATION_I);
        COMMAND_ARCHITECTURE_MAP.put("ADD", Architecture.GENERATION_I);
        COMMAND_ARCHITECTURE_MAP.put("SUB", Architecture.GENERATION_I);
        COMMAND_ARCHITECTURE_MAP.put("INC", Architecture.GENERATION_I);
        COMMAND_ARCHITECTURE_MAP.put("DEC", Architecture.GENERATION_I);
        COMMAND_ARCHITECTURE_MAP.put("NOP", Architecture.GENERATION_I);

        // Generation II - Arithmetic operations
        COMMAND_ARCHITECTURE_MAP.put("MUL", Architecture.GENERATION_II);
        COMMAND_ARCHITECTURE_MAP.put("DIV", Architecture.GENERATION_II);
        COMMAND_ARCHITECTURE_MAP.put("MOD", Architecture.GENERATION_II);
        COMMAND_ARCHITECTURE_MAP.put("SHL", Architecture.GENERATION_II);
        COMMAND_ARCHITECTURE_MAP.put("SHR", Architecture.GENERATION_II);

        // Generation III - Control flow and stack operations
        COMMAND_ARCHITECTURE_MAP.put("JMP", Architecture.GENERATION_III);
        COMMAND_ARCHITECTURE_MAP.put("JZ", Architecture.GENERATION_III);
        COMMAND_ARCHITECTURE_MAP.put("JNZ", Architecture.GENERATION_III);
        COMMAND_ARCHITECTURE_MAP.put("CALL", Architecture.GENERATION_III);
        COMMAND_ARCHITECTURE_MAP.put("RET", Architecture.GENERATION_III);
        COMMAND_ARCHITECTURE_MAP.put("PUSH", Architecture.GENERATION_III);
        COMMAND_ARCHITECTURE_MAP.put("POP", Architecture.GENERATION_III);

        // Generation IV - Advanced operations
        COMMAND_ARCHITECTURE_MAP.put("VECTOR_ADD", Architecture.GENERATION_IV);
        COMMAND_ARCHITECTURE_MAP.put("MATRIX_MUL", Architecture.GENERATION_IV);
        COMMAND_ARCHITECTURE_MAP.put("SIMD_OP", Architecture.GENERATION_IV);
    }

    /**
     * Main execution method - THIS IS WHERE VALIDATION HAPPENS
     */
    public ExecuteProgramResponse executeProgram(ExecuteProgramRequest request) {
        // 1. Load program instructions
        List<InstructionDTO> instructions = loadProgramInstructions(request.programId);

        // 2. CRITICAL: Validate architecture BEFORE execution
        ArchitectureValidationDTO validation = ArchitectureValidator.validate(
            instructions,
            request.architecture
        );

        // 3. If validation fails, return error immediately
        if (!validation.isValid()) {
            System.out.println("Architecture validation failed: " + validation.getErrorMessage());
            return new ExecuteProgramResponse(validation);
        }

        // 4. Validation passed - proceed with execution
        String executionId = startActualExecution(request);
        return new ExecuteProgramResponse(executionId);
    }

    /**
     * Load program instructions and SET THE REQUIRED ARCHITECTURE for each
     * This is CRITICAL - if you don't set requiredArchitecture, validation won't work!
     */
    private List<InstructionDTO> loadProgramInstructions(String programId) {
        List<InstructionDTO> instructions = new ArrayList<>();

        // Example: Load from your program storage
        // Replace this with your actual program loading logic
        List<String[]> rawCommands = loadRawCommands(programId);

        for (int i = 0; i < rawCommands.size(); i++) {
            String[] cmd = rawCommands.get(i);
            String instruction = cmd[0];
            String arguments = cmd.length > 1 ? cmd[1] : "";

            InstructionDTO dto = new InstructionDTO(i, instruction, arguments);

            // *** THIS IS THE KEY PART ***
            // Set the required architecture based on the command type
            Architecture required = getRequiredArchitectureForCommand(instruction);
            dto.setRequiredArchitecture(required);

            instructions.add(dto);
        }

        return instructions;
    }

    /**
     * Determine required architecture for a command
     * CUSTOMIZE THIS based on your emulator's command set
     */
    private Architecture getRequiredArchitectureForCommand(String command) {
        String normalizedCmd = command.trim().toUpperCase();

        // Look up in the map
        Architecture required = COMMAND_ARCHITECTURE_MAP.get(normalizedCmd);

        // Default to Generation I if not found
        return required != null ? required : Architecture.GENERATION_I;
    }

    /**
     * When displaying program info (before execution), also mark incompatible instructions
     */
    public ProgramInfoDTO getProgramInfo(String programId, Architecture userArchitecture) {
        // Load program details
        ProgramInfoDTO info = loadProgramInfo(programId);

        // Load instructions
        List<InstructionDTO> instructions = loadProgramInstructions(programId);

        // Mark which instructions are incompatible with user's current architecture selection
        ArchitectureValidator.markIncompatibleInstructions(instructions, userArchitecture);

        // You can add instructions to the DTO or return them separately
        // This allows the frontend to show red highlighting before execution

        return info;
    }

    // Placeholder methods - replace with your actual implementation

    private List<String[]> loadRawCommands(String programId) {
        // Replace with actual database/file loading
        List<String[]> commands = new ArrayList<>();
        commands.add(new String[]{"MOV", "R1, 10"});
        commands.add(new String[]{"DIV", "R1, 2"});  // Requires Generation II!
        return commands;
    }

    private ProgramInfoDTO loadProgramInfo(String programId) {
        // Replace with actual program loading
        return new ProgramInfoDTO();
    }

    private String startActualExecution(ExecuteProgramRequest request) {
        // Replace with actual execution logic
        return "exec-" + System.currentTimeMillis();
    }
}

