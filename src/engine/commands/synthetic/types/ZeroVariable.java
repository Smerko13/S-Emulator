package engine.commands.synthetic.types;

import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class ZeroVariable extends SyntheticCommand {

    public ZeroVariable(SInstruction instruction) {
        super(instruction);
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.levelOfExpansion = 1;
    }

    public void initializeExpandedCommands() {
    }

    @Override
    public String execute(int expansionLevel) {
        this.varible.setValue(0);
        return null;
    }

    @Override
    public String toString() {
        return varible.getName() + " <- 0";
    }
}
