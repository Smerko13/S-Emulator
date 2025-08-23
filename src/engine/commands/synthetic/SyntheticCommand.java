package engine.commands.synthetic;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
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

    public SyntheticCommand(Variable variable, String label, Command parentCommand) {
        super(variable, label, parentCommand);
        this.commandType = 'S'; // SyntheticCommand type
        this.isExpandable = true; // SyntheticCommand is expandable
        this.ExpandedCommands = new java.util.ArrayList<>();
    }

    public abstract void initializeExpandedCommands();

    public abstract String execute(int expansionLevel);

    public List<Command> getExpandedCommands() {
        return ExpandedCommands;
    }

    public String generateNewLabel() {
        int labelIndex = 1;
        for(String label : Engine.labels) {
            if (label.charAt(0) == 'L' && label.charAt(1) == labelIndex + '0') {
                labelIndex++;
            }
        }
        return "L" + labelIndex;
    }

    public String generateNewWorkVariableName() {
        int workArgIndex = 1;
        for (Variable variable : Engine.variables) {
            if (variable instanceof WorkVariable && variable.getName().charAt(1) == workArgIndex + '0') {
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
}
