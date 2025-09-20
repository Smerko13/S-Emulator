package engine.commands.base.types;

import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public class Neutral extends BaseCommand implements Serializable {
    public Neutral(SInstruction instruction) {
        super(instruction);
        this.commandName = "NEUTRAL";
        this.cycles = 0;
    }

    public Neutral(Variable variable, String newLabel3, Command parentCommand) {
        super(variable, newLabel3, parentCommand);
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
}
