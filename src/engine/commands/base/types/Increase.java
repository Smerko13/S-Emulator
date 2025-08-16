package engine.commands.base.types;

import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Increase extends BaseCommand {
    public Increase(SInstruction instruction) {
        super(instruction);
        this.commandName = "INCREASE";
        this.cycles = 1;
    }

    @Override
    public String execute(int expansionLevel) {
        // Increase the variable's value by 1
        int currentValue = varible.getValue();
        varible.setValue(currentValue + 1);
        return null;
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + varible.getName() + " + 1";
    }
}
