package engine.arguments.types;

import engine.arguments.Variable;

public class InputVariable extends Variable {

    public InputVariable(String name) {
        this.id = name.charAt(1) - '0'; // Extracting ID from the name
        this.type = 'x'; // 'I' for Input
        this.name = name;
    }

    public InputVariable(String name, int value) {
        this.id = name.charAt(1) - '0'; // Extracting ID from the name
        this.type = 'x'; // 'I' for Input
        this.name = name;
        this.value = value; // Setting the initial value
    }

    public InputVariable(InputVariable variable) {
        super(variable);
    }
}
