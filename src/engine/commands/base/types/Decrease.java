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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

public class Decrease extends BaseCommand implements Serializable {
    public Decrease(SInstruction instruction) {
        super(instruction);
        this.commandName = "DECREASE";
        this.cycles = 1;
    }

    public Decrease(Variable variable, String label, Command parentCommand) {
        super(variable, label, parentCommand);
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


}
