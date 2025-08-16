package engine.commands.base.types;

import engine.arguments.Varible;
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
}
