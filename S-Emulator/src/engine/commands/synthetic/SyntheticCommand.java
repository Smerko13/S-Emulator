package engine.commands.synthetic;

import engine.Program;
import engine.arguments.Variable;
import engine.commands.Command;
import schema.SInstruction;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public abstract class SyntheticCommand extends Command implements Serializable {
    protected List<Command> ExpandedCommands;
    protected boolean didInitialize = false;

    public SyntheticCommand(SInstruction instruction, Program program) {
        super(instruction, program);
        this.commandType = 'S'; // SyntheticCommand type
        this.isExpandable = true; // SyntheticCommand is expandable
        this.ExpandedCommands = new java.util.ArrayList<>();
    }

    public SyntheticCommand(Variable variable, String label, Command parentCommand, Program program) {
        super(variable, label, parentCommand, program);
        this.commandType = 'S'; // SyntheticCommand type
        this.isExpandable = true; // SyntheticCommand is expandable
        this.ExpandedCommands = new java.util.ArrayList<>();
    }

    public abstract void initializeExpandedCommands();

    public abstract String execute();

    public List<Command> getExpandedCommands() {
        return ExpandedCommands;
    }

    public String generateNewLabel() {
        int labelIndex = 1;
        boolean found = false;
        while (!found) {
            String currentLabel = "L" + labelIndex;
            if (!this.associatedProgram.labels.contains(currentLabel)) {
                found = true;
            } else {
                labelIndex++;
            }
        }
        return "L" + labelIndex;
    }

    protected void expandFurther() {
        for(Command cmd : this.ExpandedCommands) {
            if(cmd instanceof SyntheticCommand) {
                ((SyntheticCommand) cmd).initializeExpandedCommands();
            }
        }
    }
    public List <Command> getCommands() {
        return this.ExpandedCommands;
    }

    @Override
    public abstract boolean isValid();

    @Override
    public abstract String getTargetLabel();

    public abstract Set<Variable> getAllVariables();

    @Override
    public SyntheticCommand clone() {
        SyntheticCommand cloned = (SyntheticCommand) super.clone();
        cloned.ExpandedCommands = new java.util.ArrayList<>();
        for (Command cmd : this.ExpandedCommands) {
            cloned.ExpandedCommands.add(cmd.clone());
        }
        return cloned;
    }

    @Override
    public int getExpansionDepth() {
        return this.levelOfExpansion;
    }
}
