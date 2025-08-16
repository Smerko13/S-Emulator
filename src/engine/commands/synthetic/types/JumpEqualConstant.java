package engine.commands.synthetic.types;

import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class JumpEqualConstant extends SyntheticCommand {
    private String JEConstantLabel; // Label for the jump if condition is met
    private int constantValue; // Assuming a constant value for comparison


    public JumpEqualConstant(SInstruction instruction) {
        super(instruction);
        this.commandName = "JUMP_EQUAL_CONSTANT";
        this.cycles = 2;
        this.levelOfExpansion = 1;
        this.constantValue = Integer.parseInt(instruction.getSInstructionArguments().getSInstructionArgument().getLast().getValue());
        this.JEConstantLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String toString() {
        return "IF " + varible.getName() + " = " + constantValue + " GOTO " + JEConstantLabel;
    }
}
