package api.dto;

public class ExecutionHistoryDTO {
    public int runId;                    // Sequential execution number
    public String executionType;         // "Main Program" or "Helper Function"
    public String programFunctionName;   // Name of executed program/function
    public String architectureType;      // "I", "II", "III", "IV"
    public String executionLevel;        // "Run" or "Debug" (kept for backward compatibility)
    public int expansionDegree;          // Degree/expansion level (0 to maxDepth)
    public int finalYValue;              // Final value of variable y
    public int cpuCyclesUsed;            // Total cycles consumed

    public ExecutionHistoryDTO() {}

    public ExecutionHistoryDTO(int runId, String executionType, String programFunctionName,
                              String architectureType, String executionMode, int expansionDegree,
                              int finalYValue, int cpuCyclesUsed) {
        this.runId = runId;
        this.executionType = executionType;
        this.programFunctionName = programFunctionName;
        this.architectureType = architectureType;
        this.executionLevel = executionMode;
        this.expansionDegree = expansionDegree;
        this.finalYValue = finalYValue;
        this.cpuCyclesUsed = cpuCyclesUsed;
    }
}
