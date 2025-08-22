package engine.commands.synthetic.types;

import engine.commands.base.types.Increase;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class ConstantAssignment extends SyntheticCommand {
    private int constantValue;

    public ConstantAssignment(SInstruction instruction) {
        super(instruction);
        this.commandName = "CONSTANT_ASSIGNMENT";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        this.constantValue = Integer.parseInt(instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue());
    }

    @Override
    public void initializeExpandedCommands() {
        this.ExpandedCommands.add(new ZeroVariable(this.varible,this.label));
        for(int i = 0 ; i < this.constantValue ; i++) {
            this.ExpandedCommands.add(new Increase(this.varible, "   "));
        }
        expandFurther();
    }

    @Override
    public String execute(int expansionLevel) {
        // Assign the constant value to the variable
        varible.setValue(constantValue);
        return null; // No further action needed, just assignment
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + constantValue; // Assigning a constant value to the variable
    }
}
