package engine.commands.base;

import engine.Program;
import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.synthetic.types.Quote;
import schema.SInstruction;

import java.io.Serializable;
import java.util.Set;

public abstract class BaseCommand extends Command implements Serializable {
    public BaseCommand(SInstruction instruction, Program program) {
        super(instruction, program);
        this.isExpandable = false; // BaseCommand is not expandable
        this.commandType = 'B'; // BaseCommand type
        this.levelOfExpansion = 0;
    }

    public BaseCommand(Variable variable, String label, Command parentCommand, Program program) {
        super(variable, label, parentCommand, program);
        this.isExpandable = false; // BaseCommand is not expandable
        this.commandType = 'B'; // BaseCommand type
        this.levelOfExpansion = 0;
    }

    public BaseCommand(Command cmd, Quote quote, String label, Variable outputVar, Program program) {
        super(cmd, quote, label, outputVar, program);
    }

    @Override
    public abstract String execute();

    @Override
    public abstract boolean isValid();

    @Override
    public abstract String getTargetLabel();

    public abstract Set<Variable> getAllVariables();

    public int getExpansionDepth() {
        return this.levelOfExpansion;
    }
}
