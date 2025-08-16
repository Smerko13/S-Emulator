package engine.commands.base.types;

import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Neutral extends BaseCommand {
    public Neutral(SInstruction instruction) {
        super(instruction);
        this.commandName = "NEUTRAL";
        this.cycles = 0;
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + varible.getName();
    }
}
