package engine.arguments;

public abstract class Variable {
    protected char type;
    protected int value = 0;
    protected int id;
    protected String name;
    protected boolean isOriginal = true;

    public String getName() {
        return name;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Variable(){

    }

    public Variable(Variable variable) {
        this.type = variable.type;
        this.value = variable.value;
        this.id = variable.id;
        this.name = variable.name;
    }

    public void resetValue() {
        this.value = 0;
    }
}
