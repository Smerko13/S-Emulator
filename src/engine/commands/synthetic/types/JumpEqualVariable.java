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

public class JumpEqualVariable extends SyntheticCommand {
    private String JEVariableLabel;
    private String variableName;

    public JumpEqualVariable(SInstruction instruction) {
        super(instruction);
        this.commandName = "JUMP_EQUAL_VARIABLE";
        this.cycles = 2;
        this.levelOfExpansion = 3;
        this.JEVariableLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.variableName = instruction.getSInstructionArguments().getSInstructionArgument().getLast().getValue();
        if(this.variableName.charAt(0)=='z'){
            WorkVariable workVariable = new WorkVariable(this.variableName);
            Engine.variables.add(workVariable);
        } else if(this.variableName.charAt(0)=='x'){
            InputVariable inputVariable = new InputVariable(this.variableName);
            Engine.variables.add(inputVariable);
        }
        else {
            throw new IllegalArgumentException("Invalid variable type for comparison. Only 'x' (input) and 'z' (work) variables are allowed.");
        }
    }

    @Override
    public void initializeExpandedCommands() {
        WorkVariable newWorkVariable1 = new WorkVariable(generateNewWorkVariableName());
        Engine.variables.add(newWorkVariable1);
        WorkVariable newWorkVariable2 = new WorkVariable(generateNewWorkVariableName());
        Engine.variables.add(newWorkVariable2);
        this.ExpandedCommands.add(new Assignment(newWorkVariable1, this.label, this.variable, this));
        for(Variable v : Engine.variables) {
            if(v.getName().equals(this.variableName)) {
                this.ExpandedCommands.add(new Assignment(newWorkVariable2, "   ", v, this));
                break;
            }
        }
        String newLabel1 = generateNewLabel();
        Engine.labels.add(newLabel1);
        String newLabel2 = generateNewLabel();
        Engine.labels.add(newLabel2);
        String newLabel3 = generateNewLabel();
        Engine.labels.add(newLabel3);
        this.ExpandedCommands.add(new JumpZero(newWorkVariable1,newLabel3,newLabel2,this));
        this.ExpandedCommands.add(new JumpZero(newWorkVariable2,newLabel1,"   ",this));
        this.ExpandedCommands.add(new Decrease(newWorkVariable1,"   ",this));
        this.ExpandedCommands.add(new Decrease(newWorkVariable2,"   ",this));
        this.ExpandedCommands.add(new GotoLabel(newLabel2,this));
        this.ExpandedCommands.add(new JumpZero(newWorkVariable2,this.JEVariableLabel,newLabel3,this));
        for(Variable v : Engine.variables) {
            if(v instanceof OutputVariable){
                this.ExpandedCommands.add(new Neutral(v,newLabel1,this));
            }
        }
        expandFurther();
    }

    @Override
    public String execute() {
        int varValue = this.variable.getValue();
        int checkedValue;
        for(Variable var : Engine.variables)
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
        Engine.variables.add(newVar);
        if(newVar.getValue() == varValue) {
            // If the newly created variable's value equals the original variable's value, return the label for jumping
            return this.JEVariableLabel;
        } else {
            // If not equal, continue execution without jumping
            return null;
        }
    }

    @Override
    public String toString() {
        return "IF " + this.variable.getName() + " = " + this.variableName + " THEN JUMP TO " + JEVariableLabel;
    }
}
