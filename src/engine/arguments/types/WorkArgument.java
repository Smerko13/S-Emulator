package engine.arguments.types;

import engine.arguments.Argument;

public class WorkArgument extends Argument {

    public WorkArgument(String name) {
        this.id = name.charAt(1) - '0'; // Extracting ID from the name
        this.type = 'z'; // 'W' for Work
        this.name = name;
    }
}
