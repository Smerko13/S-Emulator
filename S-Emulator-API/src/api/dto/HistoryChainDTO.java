package api.dto;

public class HistoryChainDTO {
    private int stepNumber;
    private int commandId;
    private String commandType; // "basic" or "synthetic"
    private String commandName; // The actual command name like "DECREASE", "ASSIGNMENT"
    private String arguments;
    private String expandedFrom;
    private int level;
    private int cycles;
    private String instructionText;
    private String label; // The command's label (if any)

    // Constructors
    public HistoryChainDTO() {}

    public HistoryChainDTO(int stepNumber, String commandName, String arguments, String expandedFrom, int level) {
        this.stepNumber = stepNumber;
        this.commandName = commandName;
        this.arguments = arguments;
        this.expandedFrom = expandedFrom;
        this.level = level;
    }

    // Getters and Setters
    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public int getCommandId() {
        return commandId;
    }

    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getExpandedFrom() {
        return expandedFrom;
    }

    public void setExpandedFrom(String expandedFrom) {
        this.expandedFrom = expandedFrom;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCycles() {
        return cycles;
    }

    public void setCycles(int cycles) {
        this.cycles = cycles;
    }

    public String getInstructionText() {
        return instructionText;
    }

    public void setInstructionText(String instructionText) {
        this.instructionText = instructionText;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "HistoryChainDTO{" +
                "stepNumber=" + stepNumber +
                ", commandId=" + commandId +
                ", commandType='" + commandType + '\'' +
                ", commandName='" + commandName + '\'' +
                ", arguments='" + arguments + '\'' +
                ", expandedFrom='" + expandedFrom + '\'' +
                ", level=" + level +
                ", cycles=" + cycles +
                ", instructionText='" + instructionText + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
