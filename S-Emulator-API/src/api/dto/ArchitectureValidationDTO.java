package api.dto;

import java.util.List;

public class ArchitectureValidationDTO {
    private boolean isValid;
    private String errorMessage;
    private List<Integer> incompatibleInstructionIds;
    private Architecture requiredArchitecture;
    private Architecture providedArchitecture;

    // Constructors
    public ArchitectureValidationDTO() {}

    public ArchitectureValidationDTO(boolean isValid) {
        this.isValid = isValid;
    }

    public ArchitectureValidationDTO(boolean isValid, String errorMessage,
                                    List<Integer> incompatibleInstructionIds,
                                    Architecture requiredArchitecture,
                                    Architecture providedArchitecture) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
        this.incompatibleInstructionIds = incompatibleInstructionIds;
        this.requiredArchitecture = requiredArchitecture;
        this.providedArchitecture = providedArchitecture;
    }

    // Getters and Setters
    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<Integer> getIncompatibleInstructionIds() {
        return incompatibleInstructionIds;
    }

    public void setIncompatibleInstructionIds(List<Integer> incompatibleInstructionIds) {
        this.incompatibleInstructionIds = incompatibleInstructionIds;
    }

    public Architecture getRequiredArchitecture() {
        return requiredArchitecture;
    }

    public void setRequiredArchitecture(Architecture requiredArchitecture) {
        this.requiredArchitecture = requiredArchitecture;
    }

    public Architecture getProvidedArchitecture() {
        return providedArchitecture;
    }

    public void setProvidedArchitecture(Architecture providedArchitecture) {
        this.providedArchitecture = providedArchitecture;
    }
}
