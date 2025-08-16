package engine.commands.synthetic;

import engine.commands.Command;
import schema.SInstruction;

import java.util.List;

public abstract class SyntheticCommand extends Command {
    protected List<Command> ExpandedCommands;

    public SyntheticCommand(SInstruction instruction) {
        super(instruction);
        this.commandType = 'S'; // SyntheticCommand type
        this.isExpandable = true; // SyntheticCommand is expandable
        this.ExpandedCommands = new java.util.ArrayList<>();
    }

    public abstract void initializeExpandedCommands();

    public abstract String execute(int expansionLevel);
}
