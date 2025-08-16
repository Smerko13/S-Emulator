package engine.commands.base.types;

import engine.arguments.Varible;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Increase extends BaseCommand {

    public Increase(SInstruction instruction) {
        super(instruction);
        this.commandName = "INCREASE";
        this.cycles = 1;
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + varible.getName() + " + 1";
    }
}
