package engine.commands.synthetic.types;

import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class JumpZero extends SyntheticCommand {
    private String JZLabel;

    public JumpZero(SInstruction instruction) {
        super(instruction);
        this.commandName = "JUMP_ZERO";
        this.cycles = 2;
        this.levelOfExpansion = 1;
        JZLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String execute(int expansionLevel) {
        // Check if the variable is zero
        if (varible.getValue() == 0) {
            // If zero, return the label for jumping
            return JZLabel;
        }
        // If not zero, do nothing and continue execution
        return null;
    }

    @Override
    public String toString() {
        return "IF " + varible.getName() + " = 0 GOTO " + JZLabel;
    }
}
