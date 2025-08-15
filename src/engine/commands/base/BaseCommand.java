package engine.commands.base;

import engine.arguments.Varible;
import engine.commands.Command;

public abstract class BaseCommand extends Command {
    protected Varible varible;

    public BaseCommand() {
        super();
        this.isExpandable = false; // BaseCommand is not expandable
        this.commandType = 'B'; // BaseCommand type
        this.levelOfExpansion = 0;
    }
}
