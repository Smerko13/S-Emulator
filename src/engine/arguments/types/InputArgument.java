package engine.arguments.types;

import engine.arguments.Argument;

public class InputArgument extends Argument {

    public InputArgument(String name) {
        this.id = name.charAt(1) - '0'; // Extracting ID from the name
        this.type = 'x'; // 'I' for Input
        this.name = name;
    }
}
