package engine.commands.base;

import engine.arguments.Variable;
import engine.commands.Command;
import schema.SInstruction;

import java.io.Serializable;

public abstract class BaseCommand extends Command implements Serializable {
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

    @Override
    public abstract String execute();

    @Override
    public abstract boolean isValid();

    @Override
    public abstract String getTargetLabel();
}
