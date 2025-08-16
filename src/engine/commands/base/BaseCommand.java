package engine.commands.base;

import engine.arguments.Varible;
import engine.commands.Command;
import schema.SInstruction;

public abstract class BaseCommand extends Command {
    public BaseCommand(SInstruction instruction) {
        super(instruction);
        this.isExpandable = false; // BaseCommand is not expandable
        this.commandType = 'B'; // BaseCommand type
        this.levelOfExpansion = 0;
    }
}
