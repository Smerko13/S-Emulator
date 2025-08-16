package engine.commands.base.types;

import engine.arguments.Varible;
import engine.commands.base.BaseCommand;
import schema.SInstruction;
import schema.SInstructionArgument;

public class JumpNotZero extends BaseCommand {
    String targetLabel;

    public JumpNotZero(SInstruction instruction){
        super(instruction);
        this.commandName = "JUMP_NOT_ZERO";
        this.cycles = 2;
        SInstructionArgument arg = instruction.getSInstructionArguments().getSInstructionArgument().getFirst();
        this.targetLabel = arg.getName();
    }

    @Override
    public String toString() {
        return "IF " + varible.getName() + " != 0 THEN JUMP TO " + targetLabel;
    }
}
