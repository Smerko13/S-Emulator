package api.util;

import api.dto.Architecture;
import api.dto.ArchitectureValidationDTO;
import api.dto.InstructionDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for validating architecture compatibility with program instructions.
 * This class helps identify instructions that require a higher architecture than what's provided.
 */
public class ArchitectureValidator {

    /**
     * Validates if the provided architecture is sufficient for all instructions in the program.
     *
     * @param instructions List of instructions to validate
     * @param providedArchitecture The architecture the user wants to execute with
     * @return ArchitectureValidationDTO containing validation result and incompatible instruction IDs
     */
    public static ArchitectureValidationDTO validate(List<InstructionDTO> instructions,
                                                      Architecture providedArchitecture) {
        List<Integer> incompatibleIds = new ArrayList<>();
        Architecture highestRequired = Architecture.GENERATION_I;

        // Check each instruction for architecture compatibility
        for (InstructionDTO instruction : instructions) {
            Architecture required = instruction.getRequiredArchitecture();

            if (required != null) {
                // Update highest required architecture
                if (required.ordinal() > highestRequired.ordinal()) {
                    highestRequired = required;
                }

                // Check if this instruction is incompatible with provided architecture
                if (required.ordinal() > providedArchitecture.ordinal()) {
                    incompatibleIds.add(instruction.getId());
                    instruction.setIncompatibleArchitecture(true);
                } else {
                    instruction.setIncompatibleArchitecture(false);
                }
            }
        }

        // If there are incompatible instructions, create error response
        if (!incompatibleIds.isEmpty()) {
            String errorMessage = String.format(
                "Cannot execute program with %s. This program requires at least %s. " +
                "%d instruction(s) are incompatible with your selected architecture.",
                providedArchitecture.getDisplayName(),
                highestRequired.getDisplayName(),
                incompatibleIds.size()
            );

            return new ArchitectureValidationDTO(
                false,
                errorMessage,
                incompatibleIds,
                highestRequired,
                providedArchitecture
            );
        }

        // All instructions are compatible
        return new ArchitectureValidationDTO(true);
    }

    /**
     * Marks instructions that are incompatible with the provided architecture.
     * This is useful for displaying which instructions would fail before execution.
     *
     * @param instructions List of instructions to mark
     * @param providedArchitecture The architecture to check against
     */
    public static void markIncompatibleInstructions(List<InstructionDTO> instructions,
                                                     Architecture providedArchitecture) {
        for (InstructionDTO instruction : instructions) {
            Architecture required = instruction.getRequiredArchitecture();

            if (required != null && required.ordinal() > providedArchitecture.ordinal()) {
                instruction.setIncompatibleArchitecture(true);
            } else {
                instruction.setIncompatibleArchitecture(false);
            }
        }
    }
}

