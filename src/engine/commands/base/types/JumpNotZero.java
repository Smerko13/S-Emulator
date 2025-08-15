package engine.commands.base.types;

import engine.arguments.Varible;
import engine.commands.base.BaseCommand;
import schema.SInstruction;
import schema.SInstructionArgument;

public class JumpNotZero extends BaseCommand {
    String targetLabel;

    public JumpNotZero(SInstruction instruction){
        super();
        this.commandName = "JUMP_NOT_ZERO";
        if(instruction.getSLabel()!=null){this.label = instruction.getSLabel();}
        this.cycles = 2; // Assuming it takes 2 cycle to execute
        String var = instruction.getSVariable();
        this.varible = extractVariables(var, this.varible);
        SInstructionArgument arg = instruction.getSInstructionArguments().getSInstructionArgument().getFirst();
        this.targetLabel = arg.getName();
    }

    public JumpNotZero(Varible varible, String l1) {
        super();
        this.commandName = "JUMP_NOT_ZERO";
        this.cycles = 2; // Assuming it takes 2 cycle to execute
        this.varible = varible;
        this.targetLabel = l1; // Set the target label to the provided label
    }

    public String jumpNotZero(int V, String L) {
        if (V != 0) {
            return L; // Return label L if V is not zero
        } else {
            return "  "; // Return empty label if V is zero
        }
    }

    @Override
    public String toString() {
        return "IF " + varible.getName() + " != 0 THEN JUMP TO " + targetLabel;
    }
}
