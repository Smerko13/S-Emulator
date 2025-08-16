package engine.commands.synthetic.types;

import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class ConstantAssignment extends SyntheticCommand {
    private int constantValue;

    public ConstantAssignment(SInstruction instruction) {
        super(instruction);
        this.commandName = "CONSTANT_ASSIGNMENT";
        this.cycles = 2;
        this.levelOfExpansion = 1;
        this.constantValue = Integer.parseInt(instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue());
    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + constantValue; // Assigning a constant value to the variable
    }
}
