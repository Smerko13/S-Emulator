package engine.commands.synthetic.types;

import engine.arguments.Varible;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class Assignment extends SyntheticCommand {
    protected Varible assignedVarible;

    public Assignment(SInstruction instruction) {
        super(instruction);
        this.commandName = "ASSIGNMENT";
        this.cycles = 4;
        this.levelOfExpansion = 2;
        String assignedVar = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.assignedVarible = extractVariables(assignedVar);
    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String execute(int expansionLevel) {
        // Assign the value of the assigned variable to the variable
        if (assignedVarible != null) {
            varible.setValue(assignedVarible.getValue());
        }
        return null;
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + assignedVarible.getName();
    }
}
