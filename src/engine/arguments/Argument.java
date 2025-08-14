package engine.arguments;

public abstract class Argument {
    protected char type;
    protected int value = 0; // Default value for arguments
    protected int id;
    protected String name;

    public String getName() {
        return name;
    }
}
