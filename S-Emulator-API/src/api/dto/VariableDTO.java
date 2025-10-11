package api.dto;

public class VariableDTO {
    private String name;
    private int value;
    private String type;
    private boolean isInput;
    private boolean changed;

    // Constructors
    public VariableDTO() {}

    public VariableDTO(String name, int value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.isInput = false;
        this.changed = false;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isInput() {
        return isInput;
    }

    public void setInput(boolean input) {
        isInput = input;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public String toString() {
        return "VariableDTO{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", type='" + type + '\'' +
                ", isInput=" + isInput +
                ", changed=" + changed +
                '}';
    }
}
