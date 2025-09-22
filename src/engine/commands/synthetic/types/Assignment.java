package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.Increase;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.*;

public class Assignment extends SyntheticCommand implements Serializable {
    protected Variable assignedVariable;

    public Assignment(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "ASSIGNMENT";
        this.cycles = 4;
        this.levelOfExpansion = 2;
        String assignedVar = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.assignedVariable = extractVariables(assignedVar);
        this.associatedVariables.add(assignedVariable);
    }

    public Assignment(Variable newWorkVariable, String label, Variable assignedVariable, Command parentCommand, Engine engine) {
        super(newWorkVariable, label, parentCommand, engine);
        this.commandName = "ASSIGNMENT";
        this.cycles = 4;
        this.levelOfExpansion = 2;
        this.assignedVariable = assignedVariable;
        this.associatedVariables.add(assignedVariable);
    }

    @Override
    public void initializeExpandedCommands() {
        String newLabel1 = generateNewLabel();
        this.associatedEngine.labels.add(newLabel1);
        String newLabel2 = generateNewLabel();
        this.associatedEngine.labels.add(newLabel2);
        String newLabel3 = generateNewLabel();
        this.associatedEngine.labels.add(newLabel3);
        this.ExpandedCommands.add(new ZeroVariable(variable,this.label,this, this.associatedEngine));
        this.ExpandedCommands.add(new JumpNotZero(assignedVariable, newLabel1,"   ", this, this.associatedEngine));
        this.ExpandedCommands.add(new GotoLabel(newLabel3,this, this.associatedEngine));
        this.ExpandedCommands.add(new Decrease(assignedVariable,newLabel1, this, this.associatedEngine));
        WorkVariable newWorkVariable = new WorkVariable(generateNewWorkVariableName());
        this.associatedEngine.getVariables().add(newWorkVariable);
        this.ExpandedCommands.add(new Increase(newWorkVariable,"   ",this, this.associatedEngine));
        this.ExpandedCommands.add(new JumpNotZero(assignedVariable, newLabel1, "   ", this, this.associatedEngine));
        this.ExpandedCommands.add(new Decrease(newWorkVariable,newLabel2, this, this.associatedEngine));
        this.ExpandedCommands.add(new Increase(variable,"   ", this, this.associatedEngine));
        this.ExpandedCommands.add(new Increase(assignedVariable,"   ", this, this.associatedEngine));
        this.ExpandedCommands.add(new JumpNotZero(newWorkVariable, newLabel2, "   ", this, this.associatedEngine));
        this.ExpandedCommands.add(new Neutral(variable,newLabel3, this, this.associatedEngine));

        expandFurther();
    }

    @Override
    public String execute() {
        // Assign the value of the assigned variable to the variable
        if (assignedVariable != null) {
            variable.setValue(assignedVariable.getValue());
        }
        return null;
    }

    @Override
    public boolean isValid() {
        if( assignedVariable == null || variable == null) {
            return false;
        } else return assignedVariable.getValue() >= 0 && variable.getValue() >= 0;
    }

    @Override
    public String getTargetLabel() {
        return null;
    }

    @Override
    public Set<Variable> getAllVariables() {
        Set<Variable> vars = new HashSet<>();
        vars.add(assignedVariable);
        vars.add(this.variable);
        return vars;
    }

    @Override
    public Assignment clone() {
        Assignment cloned = (Assignment) super.clone();
        cloned.assignedVariable = this.assignedVariable != null ? this.assignedVariable.clone() : null;
        return cloned;
    }

    @Override
    public Collection<String> getAssociatedLabels() {
        return Collections.singleton(this.label);
    }

    @Override
    public String toString() {
        return variable.getName() + " <- " + assignedVariable.getName();
    }

    @Override
    public void replaceVariable(Variable variable, WorkVariable v) {
        if(this.variable.equals(variable)) {
            this.variable = v;
        }
        if(this.assignedVariable.equals(variable)) {
            this.assignedVariable = v;
        }
        this.associatedVariables.remove(variable);
        this.associatedVariables.add(v);
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {
        if(this.label.equals(lbl)) {
            this.label = newLabel;
        }
        this.associatedLabels.remove(lbl);
        this.associatedLabels.add(newLabel);
    }
}
