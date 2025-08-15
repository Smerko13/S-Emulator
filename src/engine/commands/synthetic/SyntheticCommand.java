package engine.commands.synthetic;

import engine.commands.Command;

import java.util.List;

public abstract class SyntheticCommand extends Command {
    protected engine.arguments.Varible varible;
    protected List<Command> ExpandedCommands;

    public SyntheticCommand() {
        super();
        this.commandType = 'S'; // SyntheticCommand type
        this.isExpandable = true; // SyntheticCommand is expandable
        this.ExpandedCommands = new java.util.ArrayList<>();
    }

    public abstract void initializeExpandedCommands();
}
