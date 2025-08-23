package engine.commands.base;

import engine.arguments.Variable;
import engine.commands.Command;
import schema.SInstruction;

public abstract class BaseCommand extends Command {
    public BaseCommand(SInstruction instruction) {
        super(instruction);
        this.isExpandable = false; // BaseCommand is not expandable
        this.commandType = 'B'; // BaseCommand type
        this.levelOfExpansion = 0;
    }
    public BaseCommand(Variable variable, String label, Command parentCommand) {
        super(variable, label, parentCommand);
        this.isExpandable = false; // BaseCommand is not expandable
        this.commandType = 'B'; // BaseCommand type
        this.levelOfExpansion = 0;
    }

    public abstract String execute();
}
