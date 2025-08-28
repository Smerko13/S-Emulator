package engine.commands.base.types;

import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

import java.io.Serializable;

public class Increase extends BaseCommand implements Serializable {
    public Increase(SInstruction instruction) {
        super(instruction);
        this.commandName = "INCREASE";
        this.cycles = 1;
    }

    public Increase(Variable variable, String label, Command parentCommand) {
        super(variable, label, parentCommand);
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
}
