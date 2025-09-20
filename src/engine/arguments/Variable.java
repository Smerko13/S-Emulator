package engine.arguments;

import java.io.Serializable;

public abstract class Variable implements Serializable, Cloneable {
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
        this.isOriginal = variable.isOriginal;
    }

    @Override
    public Variable clone() {
        try {
            return (Variable) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public char getType() {
        return this.type;
    }
}
