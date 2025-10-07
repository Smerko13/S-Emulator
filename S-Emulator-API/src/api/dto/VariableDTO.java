package api.dto;

public class VariableDTO {
    private String name;
    private int value;
    /** optional: "INPUT" | "WORK" | "OUTPUT" */
    private String kind;

    public VariableDTO() {}

    public String getName() { return name; }
    public int getValue() { return value; }
    public String getKind() { return kind; }

    // convenience for UI tables that used to call getCommandRepresentation()
    @Override public String toString() { return name + "=" + value; }
}
