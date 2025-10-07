package api.dto;

import java.util.List;
import java.util.Set;

public class ExecutionStateDTO {

    // Header
    private List<String> functionNames;
    private String selectedFunction;
    private int currentDegree;
    private int maxDegree;

    // Instructions
    private List<InstructionDTO> instructions;
    private Integer highlightedInstructionId; // matches controller usage

    // Variables/exec panel
    private List<VariableDTO> allVariables;
    private List<VariableDTO> inputVariables;
    private Set<String> changedVariableNames;
    private int cycles;

    // History + stats
    private List<String> traceLines;
    private StatsDTO stats;

    // Debugging state
    private boolean debugging;

    public ExecutionStateDTO() {}

    public List<String> getFunctionNames() { return functionNames; }
    public String getSelectedFunction()    { return selectedFunction; }
    public int getCurrentDegree()          { return currentDegree; }
    public int getMaxDegree()              { return maxDegree; }

    public List<InstructionDTO> getInstructions() { return instructions; }
    public Integer getHighlightedInstructionId()   { return highlightedInstructionId; }

    public List<VariableDTO> getAllVariables()     { return allVariables; }
    public List<VariableDTO> getInputVariables()   { return inputVariables; }
    public Set<String> getChangedVariableNames()   { return changedVariableNames; }
    public int getCycles()                         { return cycles; }

    public List<String> getTraceLines() { return traceLines; }
    public StatsDTO getStats()          { return stats; }

    public boolean isDebugging() { return debugging; }
}
