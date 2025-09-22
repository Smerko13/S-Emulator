package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.*;

public class JumpZero extends SyntheticCommand implements Serializable {
    private final String JZLabel;

    public JumpZero(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "JUMP_ZERO";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        JZLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.associatedLabels.add(JZLabel);
        this.associatedEngine.labels.add(JZLabel);
        this.isJumpCommand = true;
    }

    public JumpZero(WorkVariable newWorkVariable, String jeConstantLabel, String spaces, Command parentCommand, Engine engine) {
        super(newWorkVariable,spaces, parentCommand, engine);
        this.commandName = "JUMP_ZERO";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        JZLabel = jeConstantLabel;
        this.associatedLabels.add(JZLabel);
        this.associatedEngine.labels.add(JZLabel);
        this.isJumpCommand = true;
    }

    @Override
    public void initializeExpandedCommands() {
        String newLabel = this.generateNewLabel();
        this.associatedEngine.labels.add(newLabel);
        this.ExpandedCommands.add(new JumpNotZero(this.variable, newLabel, this.label, this, this.associatedEngine));
        this.ExpandedCommands.add(new GotoLabel(this.JZLabel, this, this.associatedEngine));
        for(Variable v : this.associatedEngine.getVariables()) {
            if(v instanceof OutputVariable) {
                this.ExpandedCommands.add(new Neutral(v, newLabel, this, this.associatedEngine));
            }
        }
        expandFurther();
    }

    @Override
    public String execute() {
        // Check if the variable is zero
        if (variable.getValue() == 0) {
            // If zero, return the label for jumping
            return JZLabel;
        }
        // If not zero, do nothing and continue execution
        return null;
    }

    @Override
    public boolean isValid() {
        return variable != null;
    }

    @Override
    public String getTargetLabel() {
        return JZLabel;
    }

    @Override
    public Set<Variable> getAllVariables() {
        return Collections.singleton(this.variable);
    }

    @Override
    public JumpZero clone() {
        return (JumpZero) super.clone();
    }

    @Override
    public Collection<String> getAssociatedLabels() {
        return List.of(this.label, this.JZLabel);
    }

    @Override
    public String toString() {
        return "IF " + variable.getName() + " = 0 GOTO " + JZLabel;
    }

    @Override
    public void replaceVariable(Variable variable, WorkVariable v) {
        if (this.variable.equals(variable)) {
            this.variable = v;
        }
        if (this.associatedVariables.contains(variable)) {
            this.associatedVariables.remove(variable);
            this.associatedVariables.add(v);
        }
    }
}
