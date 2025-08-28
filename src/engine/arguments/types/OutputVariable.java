package engine.arguments.types;

import engine.arguments.Variable;

import java.io.Serializable;

public class OutputVariable extends Variable implements Serializable {
    public OutputVariable() {
        this.type = 'y'; // 'O' for Output
        this.name = "y";
    }

    public OutputVariable(OutputVariable variable) {
        super(variable);
        new OutputVariable();
    }

    @Override
    public String getName() {
        return this.name; // Output variable always has the name "y"
    }
}
