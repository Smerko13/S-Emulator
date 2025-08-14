package engine.arguments.types;

import engine.arguments.Argument;

public class OutputArgument extends Argument {


    public OutputArgument() {
        this.type = 'y'; // 'O' for Output
        this.name = "y";
    }

    @Override
    public String getName() {
        return this.name; // Output argument always has the name "y"
    }
}
