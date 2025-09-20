package engine.commands.base;

import engine.Engine;
import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.synthetic.types.Quote;
import schema.SInstruction;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Set;

public abstract class BaseCommand extends Command implements Serializable {
    public BaseCommand(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.isExpandable = false; // BaseCommand is not expandable
        this.commandType = 'B'; // BaseCommand type
        this.levelOfExpansion = 0;
    }

    public BaseCommand(Variable variable, String label, Command parentCommand, Engine engine) {
        super(variable, label, parentCommand, engine);
        this.isExpandable = false; // BaseCommand is not expandable
        this.commandType = 'B'; // BaseCommand type
        this.levelOfExpansion = 0;
    }

    public BaseCommand(Command cmd, Quote quote, String label, Variable outputVar, Engine engine) {
        super(cmd, quote, label, outputVar, engine);
    }

    @Override
    public abstract String execute();

    @Override
    public abstract boolean isValid();

    @Override
    public abstract String getTargetLabel();

    public abstract Set<Variable> getAllVariables();

}
