// api/dto/ExecuteProgramResponse.java
package api.dto;

public class ExecuteProgramResponse {
    public String executionId;
    public ArchitectureValidationDTO validationError;

    public ExecuteProgramResponse() {}

    public ExecuteProgramResponse(String executionId) {
        this.executionId = executionId;
    }

    public ExecuteProgramResponse(ArchitectureValidationDTO validationError) {
        this.validationError = validationError;
    }
}
