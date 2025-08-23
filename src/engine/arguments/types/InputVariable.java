package engine.arguments.types;

import engine.arguments.Variable;

public class InputVariable extends Variable {
    private int originalValue; // Default original value

    public InputVariable(String name) {
        this.id = name.charAt(1) - '0'; // Extracting ID from the name
        this.type = 'x'; // 'I' for Input
        this.name = name;
    }

    public InputVariable(String name, int value, boolean isOriginal) {
        this.id = name.charAt(1) - '0'; // Extracting ID from the name
        this.type = 'x'; // 'I' for Input
        this.name = name;
        this.value = value; // Setting the initial value
        this.isOriginal = isOriginal;
    }

    public boolean isOriginal() {
        return this.isOriginal;
    }


    public InputVariable(InputVariable variable) {
        super(variable);
        this.originalValue = variable.originalValue;
    }

    public int getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(int originalValue) {
        this.originalValue = originalValue;
    }
}
