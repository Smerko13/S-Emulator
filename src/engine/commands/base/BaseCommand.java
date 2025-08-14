package engine.commands.base;

import engine.commands.Command;

public abstract class BaseCommand extends Command {
    public BaseCommand() {
        super();
        this.isExpandable = false; // BaseCommand is not expandable
        this.commandType = 'B'; // BaseCommand type
        this.levelOfExpansion = 0;
    }
}
