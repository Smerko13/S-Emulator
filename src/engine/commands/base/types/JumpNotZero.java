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

public class JumpNotZero extends BaseCommand implements Serializable {
    String targetLabel;

    public JumpNotZero(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "JUMP_NOT_ZERO";
        this.cycles = 2;
        this.targetLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        Engine.labels.add(targetLabel);
        this.isJumpCommand = true;
    }

    public JumpNotZero(Variable variable, String targetLabel, String label, Command parentCommand, Engine engine) {
        super(variable, label, parentCommand, engine);
        this.commandName = "JUMP_NOT_ZERO";
        this.cycles = 2;
        this.targetLabel = targetLabel;
        Engine.labels.add(targetLabel);
        this.isJumpCommand = true;
    }

    @Override
    public String execute() {
        // Check if the variable is not zero
        if (variable.getValue() != 0) {
            // If not zero, return the target label for jumping
            return targetLabel;
        }
        // If zero, do nothing and continue execution
        return null;
    }

    @Override
    public boolean isValid() {
        if(variable == null) {
            return false;
        }
        if(Objects.equals(this.label, targetLabel)) {
            return false;
        }
        return variable.getValue() >= 0;
    }

    @Override
    public String getTargetLabel() {
        return targetLabel;
    }

    @Override
    public String toString() {
        return "IF " + variable.getName() + " != 0 THEN JUMP TO " + targetLabel;
    }

    @Override
    public Set<Variable> getAllVariables() {
        return Collections.singleton(this.variable);
    }

}
