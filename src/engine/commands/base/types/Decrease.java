package engine.commands.base.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.*;

public class Decrease extends BaseCommand implements Serializable {
    public Decrease(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "DECREASE";
        this.cycles = 1;
    }

    public Decrease(Variable variable, String label, Command parentCommand, Engine engine) {
        super(variable, label, parentCommand, engine);
        this.commandName = "DECREASE";
        this.cycles = 1;
    }

    @Override
    public String toString() {
        return variable.getName() + " <- " + variable.getName() + " - 1";
    }

    @Override
    public String execute() {
        // Decrease the variable's value by 1
        int currentValue = variable.getValue();
        if(currentValue > 0) {
            variable.setValue(currentValue - 1);
        }
        return null;
    }

    @Override
    public boolean isValid() {
        if(variable == null) {
            return false;
        }
        return variable.getValue() >= 0;
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
    public Collection<String> getAssociatedLabels() {
        return Collections.singleton(this.label);
    }

    @Override
    public void replaceVariable(Variable variable, WorkVariable v) {
        if(this.variable.equals(variable)) {
            this.variable = v;
        }
        this.associatedVariables.remove(variable);
        this.associatedVariables.add(v);
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {
        if (this.label.equals(lbl)) {
            this.label = newLabel;
        }
        this.associatedLabels.remove(lbl);
        this.associatedLabels.add(newLabel);
    }
}
