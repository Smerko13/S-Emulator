package engine.commands.base.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.BaseCommand;
import engine.commands.synthetic.types.Quote;
import schema.SInstruction;

import java.io.Serializable;
import java.util.*;

public class Neutral extends BaseCommand implements Serializable {
    public Neutral(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "NEUTRAL";
        this.cycles = 0;
    }

    public Neutral(Variable variable, String newLabel3, Command parentCommand, Engine engine) {
        super(variable, newLabel3, parentCommand, engine);
        this.commandName = "NEUTRAL";
        this.cycles = 0;
    }

    public Neutral(Command cmd, Quote quote, String label, WorkVariable outputVar, Engine engine) {
        super(cmd, quote, label, outputVar, engine);
        this.commandName = "NEUTRAL";
        this.cycles = 0;
    }

    @Override
    public String execute() {
        // Neutral command does not change the variable's value
        return null;
    }

    @Override
    public String toString() {
        return variable.getName() + " <- " + variable.getName();
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
    public void replaceLabel(String lbl, String newLabel) {
        if (this.label.equals(lbl)) {
            this.label = newLabel;
            this.associatedLabels.remove(lbl);
            this.associatedLabels.add(newLabel);
        }
    }
}
