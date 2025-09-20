package engine.arguments.types;

import engine.arguments.Variable;

import java.io.Serializable;

public class OutputVariable extends Variable implements Serializable {
    boolean isOriginal = false;

    public OutputVariable() {
        this.type = 'y';
        this.name = "y";
    }

    public OutputVariable(OutputVariable variable) {
        super(variable);
        new OutputVariable();
    }

    public OutputVariable(boolean isOriginal) {
        this.type = 'y';
        this.name = "y";
        this.isOriginal = isOriginal;
    }

    @Override
    public String getName() {
        return this.name; // Output variable always has the name "y"
    }


}
