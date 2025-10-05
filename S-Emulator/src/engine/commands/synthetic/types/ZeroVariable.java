package engine.commands.synthetic.types;

import engine.Program;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.*;


public class ZeroVariable extends SyntheticCommand implements Serializable {

    public ZeroVariable(SInstruction instruction, Program program) {
        super(instruction, program);
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.levelOfExpansion = 1;
    }

    public ZeroVariable(Variable variable, String label, Command parentCommand, Program program) {
        super(variable, label, parentCommand, program);
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.levelOfExpansion = 1;
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
        String newLabel = generateNewLabel();
        this.associatedProgram.labels.add(newLabel);
        if(this.label.equals("   ")) {
            this.ExpandedCommands.add(new Decrease(this.variable,newLabel, this, this.associatedProgram));
        } else {
            this.ExpandedCommands.add(new Neutral(this.variable,this.label, this, this.associatedProgram));
            this.ExpandedCommands.add(new Decrease(this.variable,newLabel, this, this.associatedProgram));
        }
        this.ExpandedCommands.add(new JumpNotZero(this.variable,newLabel,"   ", this, this.associatedProgram));
        expandFurther();
    }

        @Override
    public String execute() {
        this.variable.setValue(0);
        return null;
    }

    @Override
    public boolean isValid() {
        return variable != null;
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
    public ZeroVariable clone() {
        return (ZeroVariable) super.clone();
    }

    @Override
    public Collection<String> getAssociatedLabels() {
        return Collections.singleton(this.label);
    }

    @Override
    public String toString() {
        return variable.getName() + " <- 0";
    }

    @Override
    public void replaceVariable(Variable variable, WorkVariable v) {
        for(Variable var : this.associatedVariables) {
            if(var.getName().equals(variable.getName())) {
                this.associatedVariables.remove(var);
                this.associatedVariables.add(v);
            }
        }
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {
        if (newLabel == null) {
            return;
        }
        if(this.label == null) {
            return;
        }
        if (this.label.equals(lbl)) {
            this.label = newLabel;
            this.associatedLabels.remove(lbl);
            this.associatedLabels.add(newLabel);
        }
    }
}
