package api.dto;

import java.util.List;

public class InstructionDTO {
    private int id;
    private String type;      // "B" / "S" or any label you use
    private int cycles;
    private String label;     // may be null/empty
    private String text;      // full instruction string for display
    private List<String> usedVariableNames; // optional

    public InstructionDTO() {}

    public int getId() { return id; }
    public String getType() { return type; }
    public int getCycles() { return cycles; }
    public String getLabel() { return label; }
    public String getText() { return text; }
    public List<String> getUsedVariableNames() { return usedVariableNames; }

    // Back-compat name some tables used:
    public String getCommandRepresentation() { return text; }
}
