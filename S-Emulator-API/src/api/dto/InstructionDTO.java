package api.dto;

import java.util.List;

public class InstructionDTO {
    private int id;
    private String instruction;
    private String arguments;
    private boolean highlighted;
    private String type;
    private int cycles;
    private String label;
    private String text;
    private List<HistoryChainDTO> historyChain;
    private boolean incompatibleArchitecture;
    private Architecture requiredArchitecture;

    // Constructors
    public InstructionDTO() {}

    public InstructionDTO(int id, String instruction, String arguments) {
        this.id = id;
        this.instruction = instruction;
        this.arguments = arguments;
        this.highlighted = false;
        this.type = "Command"; // Default type
        this.cycles = 1; // Default cycles
        this.label = null; // Default no label
        this.text = instruction; // Default text is the instruction
        this.incompatibleArchitecture = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
        if (this.text == null) {
            this.text = instruction; // Keep text in sync
        }
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCycles() {
        return cycles;
    }

    public void setCycles(int cycles) {
        this.cycles = cycles;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<HistoryChainDTO> getHistoryChain() {
        return historyChain;
    }

    public void setHistoryChain(List<HistoryChainDTO> historyChain) {
        this.historyChain = historyChain;
    }

    public boolean isIncompatibleArchitecture() {
        return incompatibleArchitecture;
    }

    public void setIncompatibleArchitecture(boolean incompatibleArchitecture) {
        this.incompatibleArchitecture = incompatibleArchitecture;
    }

    public Architecture getRequiredArchitecture() {
        return requiredArchitecture;
    }

    public void setRequiredArchitecture(Architecture requiredArchitecture) {
        this.requiredArchitecture = requiredArchitecture;
    }

    @Override
    public String toString() {
        return "InstructionDTO{" +
                "id=" + id +
                ", instruction='" + instruction + '\'' +
                ", arguments='" + arguments + '\'' +
                ", type='" + type + '\'' +
                ", cycles=" + cycles +
                ", label='" + label + '\'' +
                ", text='" + text + '\'' +
                ", highlighted=" + highlighted +
                ", incompatibleArchitecture=" + incompatibleArchitecture +
                ", requiredArchitecture=" + requiredArchitecture +
                ", historyChain=" + historyChain +
                '}';
    }
}
