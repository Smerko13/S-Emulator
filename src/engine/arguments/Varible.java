package engine.arguments;

public abstract class Varible {
    protected char type;
    protected int value = 0; // Default value for varibles
    protected int id;
    protected String name;

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }
}
