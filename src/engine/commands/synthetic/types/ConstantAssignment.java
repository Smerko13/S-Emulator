package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.base.types.Increase;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

public class ConstantAssignment extends SyntheticCommand implements Serializable {
    private final int constantValue;

    public ConstantAssignment(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "CONSTANT_ASSIGNMENT";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        this.constantValue = Integer.parseInt(instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue());
    }

    @Override
    public void initializeExpandedCommands() {
        this.ExpandedCommands.add(new ZeroVariable(this.variable,this.label,this, this.associatedEngine));
        for(int i = 0 ; i < this.constantValue ; i++) {
            this.ExpandedCommands.add(new Increase(this.variable, "   ",this, this.associatedEngine));
        }
        expandFurther();
    }

    @Override
    public String execute() {
        // Assign the constant value to the variable
        variable.setValue(constantValue);
        return null; // No further action needed, just assignment
    }

    @Override
    public boolean isValid() {
        if(variable == null) {
            return false;
        }
        return constantValue >= 0 && variable.getValue() >= 0;
    }

    @Override
    public String getTargetLabel() {
        return null;
    }

    @Override
    public Set<Variable> getAllVariables() {
        return Collections.singleton(this.variable);
    }

    @Override
    public ConstantAssignment clone() {
        return (ConstantAssignment) super.clone();
    }

    @Override
    public String toString() {
        return variable.getName() + " <- " + constantValue; // Assigning a constant value to the variable
    }
}
