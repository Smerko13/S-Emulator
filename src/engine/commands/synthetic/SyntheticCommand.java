package engine.commands.synthetic;

import engine.Engine;
import engine.arguments.Varible;
import engine.arguments.types.WorkVarible;
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

    public SyntheticCommand(Varible varible, String label) {
        super(varible, label);
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
        String allLabels = Engine.labels;
        String newLabel = "L";
        int labelIndex = 1;
        boolean labelExists = true;
        while (labelExists) {
            labelExists = allLabels.contains(newLabel + labelIndex);
            if (labelExists) {
                labelIndex++;
            } else {
                newLabel += labelIndex;
            }
        }
        Engine.labels += newLabel + " ";
        return newLabel;
    }

    public String generateNewWorkVaribleName() {
        int workArgIndex = 1;
        for (Varible variable : Engine.varibles) {
            if (variable instanceof WorkVarible && variable.getName().charAt(1) == workArgIndex + '0') {
                workArgIndex++;
            }
        }
        return "z" + workArgIndex;
    }
}
