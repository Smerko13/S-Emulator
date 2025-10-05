package engine.commands.synthetic.types;

import engine.Program;
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
    private String JZLabel;

    public JumpZero(SInstruction instruction, Program program) {
        super(instruction, program);
        this.commandName = "JUMP_ZERO";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        JZLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.associatedLabels.add(JZLabel);
        this.associatedProgram.labels.add(JZLabel);
        this.isJumpCommand = true;
    }

    public JumpZero(WorkVariable newWorkVariable, String jeConstantLabel, String spaces, Command parentCommand, Program program) {
        super(newWorkVariable,spaces, parentCommand, program);
        this.commandName = "JUMP_ZERO";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        JZLabel = jeConstantLabel;
        this.associatedLabels.add(JZLabel);
        this.associatedProgram.labels.add(JZLabel);
        this.isJumpCommand = true;
    }

    @Override
    public void initializeExpandedCommands() {
        if(this.didInitialize) {
            this.getExpandedCommands().clear();
            expansionLogic();
            return;
        }
        expansionLogic();
        this.didInitialize = true;
    }

    private void expansionLogic() {
        String newLabel = this.generateNewLabel();
        this.associatedProgram.labels.add(newLabel);
        this.ExpandedCommands.add(new JumpNotZero(this.variable, newLabel, this.label, this, this.associatedProgram));
        this.ExpandedCommands.add(new GotoLabel(this.JZLabel, this, this.associatedProgram));
        for(Variable v : this.associatedProgram.getVariables()) {
            if(v instanceof OutputVariable) {
                this.ExpandedCommands.add(new Neutral(v, newLabel, this, this.associatedProgram));
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
    public void replaceLabel(String lbl, String newLabel) {
        if (newLabel == null) {
            return;
        }
        if(this.label != null) {
            if (this.label.equals(lbl)) {
                this.label = newLabel;
                this.associatedLabels.remove(lbl);
                this.associatedLabels.add(newLabel);
            }
        }
        if(this.JZLabel != null) {
            if (this.JZLabel.equals(lbl)) {
                this.JZLabel = newLabel;
                this.associatedLabels.remove(lbl);
                this.associatedLabels.add(newLabel);
            }
        }
    }
}
