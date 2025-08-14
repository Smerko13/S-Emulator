package engine.commands.base.types;

import engine.Engine;
import engine.arguments.Argument;
import engine.arguments.types.InputArgument;
import engine.arguments.types.OutputArgument;
import engine.arguments.types.WorkArgument;
import engine.commands.base.BaseCommand;
import schema.SInstruction;
import schema.SInstructionArgument;

public class JumpNotZero extends BaseCommand {
    String targetLabel;

    public JumpNotZero(SInstruction instruction){
        super();
        this.commandName = "JUMP_NOT_ZERO";
        if(instruction.getSLabel()!=null){this.label = instruction.getSLabel();};
        this.cycles = 2; // Assuming it takes 1 cycle to execute
        String var = instruction.getSVariable();
        if (var.charAt(0) == 'x') {
            InputArgument inputArgument = new InputArgument(var);
            boolean exists = false;
            for(Argument arg : Engine.arguments) {
                if (arg.getName().equals(inputArgument.getName())) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                this.arguments.add(inputArgument);
            } else {
                Engine.arguments.add(inputArgument);
                this.arguments.add(inputArgument);
            }
        } else if (var.charAt(0) == 'z') {
            WorkArgument workArgument = new WorkArgument(var);
            boolean exists = false;
            for(Argument arg : Engine.arguments) {
                if (arg.getName().equals(workArgument.getName())) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                this.arguments.add(workArgument);
            } else {
                Engine.arguments.add(workArgument);
                this.arguments.add(workArgument);
            }
        }
        else if (var.charAt(0) == 'y') {
            OutputArgument inputArgument = new OutputArgument();
            boolean exists = false;
            for(Argument arg : Engine.arguments) {
                if (arg.getName().equals(inputArgument.getName())) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                this.arguments.add(inputArgument);
            } else {
                Engine.arguments.add(inputArgument);
                this.arguments.add(inputArgument);
            }
        }
        SInstructionArgument sArgument = instruction.getSInstructionArguments().getSInstructionArgument().getFirst();
        this.targetLabel = sArgument.getName();
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
        return "IF " + arguments.getFirst().getName() + " != 0 THEN JUMP TO " + targetLabel;
    }
}
