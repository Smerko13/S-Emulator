package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.Increase;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.*;

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
        if(this.didInitialize) {
            this.getExpandedCommands().clear();
            expansionLogic();
            return;
        }
        expansionLogic();
        this.didInitialize = true;
    }

    private void expansionLogic() {
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
    public Collection<String> getAssociatedLabels() {
        return Collections.singleton(this.label);
    }

    @Override
    public String toString() {
        return variable.getName() + " <- " + constantValue; // Assigning a constant value to the variable
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {
        if (newLabel == null) {
            return;
        }
        if(this.label == null) return;
        if (this.label.equals(lbl)) {
            this.label = newLabel;
            this.associatedLabels.remove(lbl);
            this.associatedLabels.add(newLabel);
        }
    }
}
