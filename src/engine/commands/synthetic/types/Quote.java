package engine.commands.synthetic.types;

import engine.arguments.Variable;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.util.Set;

public class Quote extends SyntheticCommand {
    String functionName;
    String functionArguments;


    public Quote(SInstruction instruction) {
        super(instruction);
        this.commandName = "QUOTE";
        this.cycles = 5;
        this.variable = instruction.getSVariable() != null ? extractVariables(instruction.getSVariable()) : null;
        this.functionName = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.functionArguments = instruction.getSInstructionArguments().getSInstructionArgument().get(1).getValue();
    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String execute() {
        return "";
    }

    @Override
    public boolean isValid() {//need to fix
        return true;
    }

    @Override
    public String getTargetLabel() {
        return "";
    }

    @Override
    public Set<Variable> getAllVariables() {
        return Set.of();
    }

    @Override
    public String toString() {
        return this.variable.getName() + " <- (" + functionName + "(" + functionArguments + "))";
    }
}
