package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class JumpEqualVariable extends SyntheticCommand implements Serializable {
    private String JEVariableLabel;
    private String variableName;

    public JumpEqualVariable(Variable targetVariable, Variable fucValue, String targetLabel,Command parentCommand, Engine engine) {
        super(targetVariable,"   ", parentCommand, engine);
        this.commandName = "JUMP_EQUAL_VARIABLE";
        this.cycles = 2;
        this.levelOfExpansion = 3;
        this.JEVariableLabel = targetLabel;
        this.associatedLabels.add(JEVariableLabel);
        this.associatedEngine.labels.add(JEVariableLabel);
        this.variableName = fucValue.getName();
        this.isJumpCommand = true;
    }

    public JumpEqualVariable(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "JUMP_EQUAL_VARIABLE";
        this.cycles = 2;
        this.levelOfExpansion = 3;
        this.JEVariableLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.associatedLabels.add(JEVariableLabel);
        this.associatedEngine.labels.add(JEVariableLabel);
        this.variableName = instruction.getSInstructionArguments().getSInstructionArgument().getLast().getValue();
        if(this.variableName.charAt(0)=='z'){
            WorkVariable workVariable = new WorkVariable(this.variableName);
            this.associatedEngine.getVariables().add(workVariable);
            this.associatedVariables.add(workVariable);
        } else if(this.variableName.charAt(0)=='x'){
            if(checkIfVariableExists(this.variableName)) {
                this.associatedVariables.add(getExistingVariableByName(this.variableName));
            } else {
                InputVariable inputVariable = new InputVariable(this.variableName);
                this.associatedEngine.getVariables().add(inputVariable);
                this.associatedVariables.add(inputVariable);
            }
        }
        else {
            throw new IllegalArgumentException("Invalid variable type for comparison. Only 'x' (input) and 'z' (work) variables are allowed.");
        }
        this.isJumpCommand = true;
    }

    private Variable getExistingVariableByName(String variableName) {
        for (Variable var : this.associatedEngine.getVariables()) {
            if (var.getName().equals(variableName)) {
                return var;
            }
        }
        return null; // This line should never be reached if the method is used correctly
    }

    private boolean checkIfVariableExists(String variableName) {
        for (Variable var : this.associatedEngine.getVariables()) {
            if (var.getName().equals(variableName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initializeExpandedCommands() {
        WorkVariable newWorkVariable1 = new WorkVariable(generateNewWorkVariableName());
        this.associatedEngine.getVariables().add(newWorkVariable1);
        WorkVariable newWorkVariable2 = new WorkVariable(generateNewWorkVariableName());
        this.associatedEngine.getVariables().add(newWorkVariable2);
        this.ExpandedCommands.add(new Assignment(newWorkVariable1, this.label, this.variable, this, this.associatedEngine));
        for(Variable v : this.associatedEngine.getVariables()) {
            if(v.getName().equals(this.variableName)) {
                this.ExpandedCommands.add(new Assignment(newWorkVariable2, "   ", v, this, this.associatedEngine));
                break;
            }
        }
        String newLabel1 = generateNewLabel();
        this.associatedEngine.labels.add(newLabel1);
        String newLabel2 = generateNewLabel();
        this.associatedEngine.labels.add(newLabel2);
        String newLabel3 = generateNewLabel();
        this.associatedEngine.labels.add(newLabel3);
        this.ExpandedCommands.add(new JumpZero(newWorkVariable1,newLabel3,newLabel2,this,this.associatedEngine));
        this.ExpandedCommands.add(new JumpZero(newWorkVariable2,newLabel1,"   ",this,this.associatedEngine));
        this.ExpandedCommands.add(new Decrease(newWorkVariable1,"   ",this, this.associatedEngine));
        this.ExpandedCommands.add(new Decrease(newWorkVariable2,"   ",this, this.associatedEngine));
        this.ExpandedCommands.add(new GotoLabel(newLabel2,this,this.associatedEngine));
        this.ExpandedCommands.add(new JumpZero(newWorkVariable2,this.JEVariableLabel,newLabel3,this,this.associatedEngine));
        for(Variable v : this.associatedEngine.getVariables()) {
            if(v instanceof OutputVariable){
                this.ExpandedCommands.add(new Neutral(v,newLabel1,this,this.associatedEngine));
            }
        }
        expandFurther();
    }

    @Override
    public String execute() {
        int varValue = this.variable.getValue();
        int checkedValue;
        for(Variable var : this.associatedEngine.getVariables())
        {
            if(var.getName().equals(this.variableName)) {
                checkedValue = var.getValue();
                if (varValue == checkedValue) {
                    // If the variable's value equals the checked variable's value, return the label for jumping
                    return this.JEVariableLabel;
                } else {
                    // If not equal, continue execution without jumping
                    return null;
                }
            }
        }
        // If the variable with the specified name is not found, create it with a value of 0
        String newVarName = this.variableName;
        Variable newVar = extractVariables(newVarName);
        this.associatedEngine.getVariables().add(newVar);
        if(newVar.getValue() == varValue) {
            // If the newly created variable's value equals the original variable's value, return the label for jumping
            return this.JEVariableLabel;
        } else {
            // If not equal, continue execution without jumping
            return null;
        }
    }

    @Override
    public boolean isValid() {
        if(variable == null || this.variableName == null) {
            return false;
        }
        if(associatedVariables.isEmpty()) {
            return false;
        } else {
            for (Variable var : associatedVariables) {
                if(var.getValue() < 0) {
                    return false;
                }
            }
        }
        return variable.getValue() >= 0;
    }

    @Override
    public String getTargetLabel() {
        return JEVariableLabel;
    }

    @Override
    public Set<Variable> getAllVariables() {
        Set<Variable> variables = new java.util.HashSet<>();
        variables.add(variable);
        for(Variable var : this.associatedEngine.getVariables()) {
            if(var.getName().equals(this.variableName)) {
                variables.add(var);
                break;
            }
        }
        return variables;
    }

    @Override
    public JumpEqualVariable clone() {
        return (JumpEqualVariable) super.clone();
    }

    @Override
    public Collection<String> getAssociatedLabels() {
        return List.of(this.label, this.JEVariableLabel);
    }

    @Override
    public String toString() {
        return "IF " + this.variable.getName() + " = " + this.variableName + " GOTO " + JEVariableLabel;
    }

    @Override
    public void replaceVariable(Variable variable, WorkVariable v) {
        for(Variable var : this.associatedVariables) {
            if(var.getName().equals(variable.getName())) {
                this.associatedVariables.remove(var);
                this.associatedVariables.add(v);
            }
        }
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {
        if (this.label.equals(lbl)) {
            this.label = newLabel;
        }
        if (this.JEVariableLabel.equals(lbl)) {
            this.JEVariableLabel = newLabel;
        }
        this.associatedLabels.remove(lbl);
        this.associatedLabels.add(newLabel);
    }
}
