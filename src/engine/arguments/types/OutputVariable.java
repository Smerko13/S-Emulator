package engine.arguments.types;

import engine.arguments.Variable;

public class OutputVariable extends Variable {
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
