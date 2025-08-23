package engine.commands.base.types;

import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.base.BaseCommand;
import schema.SInstruction;
public class JumpNotZero extends BaseCommand {
    String targetLabel;

    public JumpNotZero(SInstruction instruction){
        super(instruction);
        this.commandName = "JUMP_NOT_ZERO";
        this.cycles = 2;
        this.targetLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
    }

    public JumpNotZero(Variable variable, String targetLabel, String label, Command parentCommand) {
        super(variable, label, parentCommand);
        this.commandName = "JUMP_NOT_ZERO";
        this.cycles = 2;
        this.targetLabel = targetLabel;
    }

    @Override
    public String execute() {
        // Check if the variable is not zero
        if (variable.getValue() != 0) {
            // If not zero, return the target label for jumping
            return targetLabel;
        }
        // If zero, do nothing and continue execution
        return null;
    }

    @Override
    public String toString() {
        return "IF " + variable.getName() + " != 0 THEN JUMP TO " + targetLabel;
    }
}
