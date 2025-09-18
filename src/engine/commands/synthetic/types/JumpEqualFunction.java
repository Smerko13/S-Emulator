package engine.commands.synthetic.types;

import engine.arguments.Variable;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.util.Set;

public class JumpEqualFunction extends SyntheticCommand {

    public JumpEqualFunction(SInstruction instruction) {
        super(instruction);

    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String execute() {
        return "";
    }

    @Override
    public boolean isValid() { //need to fix
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
}
