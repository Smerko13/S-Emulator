package engine.commands.synthetic;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import schema.SInstruction;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public abstract class SyntheticCommand extends Command implements Serializable {
    protected List<Command> ExpandedCommands;

    public SyntheticCommand(SInstruction instruction) {
        super(instruction);
        this.commandType = 'S'; // SyntheticCommand type
        this.isExpandable = true; // SyntheticCommand is expandable
        this.ExpandedCommands = new java.util.ArrayList<>();
    }

    public SyntheticCommand(Variable variable, String label, Command parentCommand) {
        super(variable, label, parentCommand);
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
            if (!Engine.labels.contains(currentLabel)) {
                found = true;
            } else {
                labelIndex++;
            }
        }
        return "L" + labelIndex;
    }

    public String generateNewWorkVariableName() {
        int workArgIndex = 1;
        boolean found = false;
        while (!found) {
            String currentWorkVarName = "z" + workArgIndex;
            if (!Engine.variables.stream().anyMatch(var -> var.getName().equals(currentWorkVarName))) {
                found = true;
            } else {
                workArgIndex++;
            }
        }
        return "z" + workArgIndex;
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
}
