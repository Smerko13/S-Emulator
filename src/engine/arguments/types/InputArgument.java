package engine.arguments.types;

import engine.arguments.Argument;

public class InputArgument extends Argument {
    private static int idCounter = 0;
    private int id;
    private int value = 0;

    public InputArgument() {
        super();
        this.id = idCounter++;
    }
}
