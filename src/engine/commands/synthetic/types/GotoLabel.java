package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.Increase;
import engine.commands.base.types.JumpNotZero;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class GotoLabel extends SyntheticCommand implements Serializable {
    private String gototLabel;

    public GotoLabel(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "GOTO_LABEL";
        this.cycles = 1;
        this.levelOfExpansion = 1;
        gototLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.associatedLabels.add(gototLabel);
        this.associatedEngine.labels.add(gototLabel);
        this.variable = createNewWorkVariable();
        this.associatedVariables.add(variable);
        this.isJumpCommand = true;
    }
    
    public GotoLabel(String gotoLabel, Command parentCommand, Engine engine) {
        super(null,"   ", parentCommand, engine);
        this.commandName = "GOTO_LABEL";
        this.cycles = 1;
        this.levelOfExpansion = 1;
        this.gototLabel = gotoLabel;
        this.associatedLabels.add(gototLabel);
        this.associatedEngine.labels.add(gototLabel);
        this.variable = createNewWorkVariable();
        this.associatedVariables.add(variable);
        this.isJumpCommand = true;
    }

    private Variable createNewWorkVariable() {
        String workVarName = generateNewWorkVariableName();
        WorkVariable workVariable = new WorkVariable(workVarName);
        this.associatedEngine.getVariables().add(workVariable);
        workVariable.setForGotoLabel(true);
        return workVariable;
    }

    @Override
    public void initializeExpandedCommands() {
        this.ExpandedCommands.add(new Increase(this.variable, this.label,this,this.associatedEngine));
        this.ExpandedCommands.add(new JumpNotZero(this.variable,this.gototLabel, "   ",this, this.associatedEngine));
    }

    @Override
    public String execute() {
        this.variable.setValue(this.variable.getValue() + 1);
        return gototLabel;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String getTargetLabel() {
        return gototLabel;
    }

    @Override
    public Set<Variable> getAllVariables() {
        return null;
    }

    @Override
    public GotoLabel clone() {
        return (GotoLabel) super.clone();
    }

    @Override
    public Collection<String> getAssociatedLabels() {
        return List.of(this.gototLabel, this.label);
    }

    @Override
    public String toString() {
        return "GOTO " + gototLabel;
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {
        if (this.label.equals(lbl)) {
            this.label = newLabel;
        }
        if (this.gototLabel.equals(lbl)) {
            this.gototLabel = newLabel;
        }
        this.associatedLabels.remove(lbl);
        this.associatedLabels.add(newLabel);
    }
}
