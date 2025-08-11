package engine.commands.synthetic;

import engine.commands.Command;

public abstract class SyntheticCommand extends Command {
    public SyntheticCommand() {
        super();
        this.commandType = 'S'; // SyntheticCommand type
        this.isExpandable = true; // SyntheticCommand is expandable

    }
}
