package engine.commands.base.types;

import engine.commands.base.BaseCommand;
import schema.SInstruction;
import schema.SInstructionArgument;

public class JumpNotZero extends BaseCommand {
    String targetLabel;

    public JumpNotZero(SInstruction instruction){
        super(instruction);
        this.commandName = "JUMP_NOT_ZERO";
        this.cycles = 2;
        this.targetLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
    }

    @Override
    public String execute(int expansionLevel) {
        // Check if the variable is not zero
        if (varible.getValue() != 0) {
            // If not zero, return the target label for jumping
            return targetLabel;
        }
        // If zero, do nothing and continue execution
        return null;
    }

    @Override
    public String toString() {
        return "IF " + varible.getName() + " != 0 THEN JUMP TO " + targetLabel;
    }
}
