// api/dto/ExecutionSummary.java
package api.dto;

public class ExecutionSummary {
    public String executionId;
    public String runType;        // "Program" | "Function"
    public String name;           // program/function name
    public String architecture;   // e.g., "x86"
    public int expansionLevel;
    public int cyclesUsed;
    public String output;         // "OK", "ERROR", etc.

    public ExecutionSummary() {}
    public ExecutionSummary(String executionId, String runType, String name,
                            String architecture, int expansionLevel,
                            int cyclesUsed, String output) {
        this.executionId = executionId; this.runType = runType; this.name = name;
        this.architecture = architecture; this.expansionLevel = expansionLevel;
        this.cyclesUsed = cyclesUsed; this.output = output;
    }
}
