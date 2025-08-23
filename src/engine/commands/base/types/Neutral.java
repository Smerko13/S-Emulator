package engine.commands.base.types;

import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Neutral extends BaseCommand {
    public Neutral(SInstruction instruction) {
        super(instruction);
        this.commandName = "NEUTRAL";
        this.cycles = 0;
    }

    public Neutral(Variable variable, String newLabel3, Command parentCommand) {
        super(variable, newLabel3, parentCommand);
        this.commandName = "NEUTRAL";
        this.cycles = 0;
    }

    @Override
    public String execute() {
        // Neutral command does not change the variable's value
        return null;
    }

    @Override
    public String toString() {
        return variable.getName() + " <- " + variable.getName();
    }
}
