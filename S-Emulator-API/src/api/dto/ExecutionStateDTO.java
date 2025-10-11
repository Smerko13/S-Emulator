package api.dto;

import java.util.List;
import java.util.Set;

public class ExecutionStateDTO {
    private String selectedFunction;
    private int currentDegree;
    private int maxDegree;
    private int cycles;
    private boolean debugging;
    private List<String> functionNames;
    private List<InstructionDTO> instructions;
    private Integer highlightedInstructionId;
    private List<VariableDTO> allVariables;
    private List<VariableDTO> inputVariables;
    private Set<String> changedVariableNames;
    private List<String> traceLines;

    // Constructors
    public ExecutionStateDTO() {}

    // Getters and Setters
    public String getSelectedFunction() {
        return selectedFunction;
    }

    public void setSelectedFunction(String selectedFunction) {
        this.selectedFunction = selectedFunction;
    }

    public int getCurrentDegree() {
        return currentDegree;
    }

    public void setCurrentDegree(int currentDegree) {
        this.currentDegree = currentDegree;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    public int getCycles() {
        return cycles;
    }

    public void setCycles(int cycles) {
        this.cycles = cycles;
    }

    public boolean isDebugging() {
        return debugging;
    }

    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    public List<String> getFunctionNames() {
        return functionNames;
    }

    public void setFunctionNames(List<String> functionNames) {
        this.functionNames = functionNames;
    }

    public List<InstructionDTO> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<InstructionDTO> instructions) {
        this.instructions = instructions;
    }

    public Integer getHighlightedInstructionId() {
        return highlightedInstructionId;
    }

    public void setHighlightedInstructionId(Integer highlightedInstructionId) {
        this.highlightedInstructionId = highlightedInstructionId;
    }

    public List<VariableDTO> getAllVariables() {
        return allVariables;
    }

    public void setAllVariables(List<VariableDTO> allVariables) {
        this.allVariables = allVariables;
    }

    public List<VariableDTO> getInputVariables() {
        return inputVariables;
    }

    public void setInputVariables(List<VariableDTO> inputVariables) {
        this.inputVariables = inputVariables;
    }

    public Set<String> getChangedVariableNames() {
        return changedVariableNames;
    }

    public void setChangedVariableNames(Set<String> changedVariableNames) {
        this.changedVariableNames = changedVariableNames;
    }

    public List<String> getTraceLines() {
        return traceLines;
    }

    public void setTraceLines(List<String> traceLines) {
        this.traceLines = traceLines;
    }
}
