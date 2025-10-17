package api.dto;

import java.util.List;

/**
 * DTO for detailed execution information including all variables at completion
 */
public class ExecutionDetailsDTO {
    public int runId;
    public String executionType;
    public String programFunctionName;
    public String architectureType;
    public String executionLevel;
    public int cpuCyclesUsed;
    public List<VariableDTO> finalVariables;

    public ExecutionDetailsDTO() {}

    public ExecutionDetailsDTO(int runId, String executionType, String programFunctionName,
                              String architectureType, String executionLevel,
                              int cpuCyclesUsed, List<VariableDTO> finalVariables) {
        this.runId = runId;
        this.executionType = executionType;
        this.programFunctionName = programFunctionName;
        this.architectureType = architectureType;
        this.executionLevel = executionLevel;
        this.cpuCyclesUsed = cpuCyclesUsed;
        this.finalVariables = finalVariables;
    }
}

