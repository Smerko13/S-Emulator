package engine.commands.synthetic.types;

import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class JumpEqualVariable extends SyntheticCommand {
    private String JEVariableLabel;
    private String variableName;

    public JumpEqualVariable(SInstruction instruction) {
        super(instruction);
        this.commandName = "JUMP_EQUAL_VARIABLE";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        this.JEVariableLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.variableName = instruction.getSInstructionArguments().getSInstructionArgument().getLast().getValue();

    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String toString() {
        return "IF " + this.varible.getName() + " = " + this.variableName + " THEN JUMP TO " + JEVariableLabel;
    }
}
