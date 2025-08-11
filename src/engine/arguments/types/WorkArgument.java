package engine.arguments.types;

import engine.arguments.Argument;

public class WorkArgument extends Argument {
    private static int idCounter = 0;
    private int id;
    private int value = 0;

    public WorkArgument() {
        super();
        this.id = idCounter++;
    }
}
