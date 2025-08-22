package engine.arguments.types;

import engine.arguments.Varible;

public class OutputVarible extends Varible {
    public OutputVarible() {
        this.type = 'y'; // 'O' for Output
        this.name = "y";
    }

    public OutputVarible(OutputVarible varible) {
        super(varible);
        new OutputVarible();
    }

    @Override
    public String getName() {
        return this.name; // Output variable always has the name "y"
    }
}
