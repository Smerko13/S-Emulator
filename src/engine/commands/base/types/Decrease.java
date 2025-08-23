package engine.commands.base.types;

import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Decrease extends BaseCommand {
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
        variable.setValue(currentValue - 1);
        return null;
    }
}
