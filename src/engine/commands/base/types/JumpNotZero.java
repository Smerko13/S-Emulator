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
    public String toString() {
        return "IF " + varible.getName() + " != 0 THEN JUMP TO " + targetLabel;
    }
}
