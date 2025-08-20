package engine.commands.base.types;

import engine.arguments.Varible;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Neutral extends BaseCommand {
    public Neutral(SInstruction instruction) {
        super(instruction);
        this.commandName = "NEUTRAL";
        this.cycles = 0;
    }

    public Neutral(Varible varible, String newLabel3) {
        super(varible, newLabel3);
        this.commandName = "NEUTRAL";
        this.cycles = 0;
    }

    @Override
    public String execute(int expansionLevel) {
        // Neutral command does not change the variable's value
        return null;
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + varible.getName();
    }
}
