package engine.commands.base.types;

import engine.Program;
import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.*;

public class Increase extends BaseCommand implements Serializable {
    public Increase(SInstruction instruction, Program program) {
        super(instruction, program);
        this.commandName = "INCREASE";
        this.cycles = 1;
    }

    public Increase(Variable variable, String label, Command parentCommand, Program program) {
        super(variable, label, parentCommand, program);
        this.commandName = "INCREASE";
        this.cycles = 1;
    }

    @Override
    public String execute() {
        // Increase the variable's value by 1
        int currentValue = variable.getValue();
        variable.setValue(currentValue + 1);
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
    public String toString() {
        return variable.getName() + " <- " + variable.getName() + " + 1";
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
