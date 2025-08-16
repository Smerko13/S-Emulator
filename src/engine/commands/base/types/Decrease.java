package engine.commands.base.types;

import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Decrease extends BaseCommand {
    public Decrease(SInstruction instruction) {
        super(instruction);
        this.commandName = "DECREASE";
        this.cycles = 1;
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + varible.getName() + " - 1";
    }

    @Override
    public String execute(int expansionLevel) {
        // Decrease the variable's value by 1
        int currentValue = varible.getValue();
        varible.setValue(currentValue - 1);
        return null;
    }
}
