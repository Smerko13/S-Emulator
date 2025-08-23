package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Varible;
import engine.arguments.types.InputVarible;
import engine.arguments.types.OutputVarible;
import engine.arguments.types.WorkVarible;
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
            WorkVarible workVarible = new WorkVarible(this.variableName);
            Engine.varibles.add(workVarible);
        } else if(this.variableName.charAt(0)=='x'){
            InputVarible inputVarible = new InputVarible(this.variableName);
            Engine.varibles.add(inputVarible);
        }
        else {
            throw new IllegalArgumentException("Invalid variable type for comparison. Only 'x' (input) and 'z' (work) variables are allowed.");
        }
    }

    @Override
    public void initializeExpandedCommands() {
        WorkVarible newWorkVarible1 = new WorkVarible(generateNewWorkVaribleName());
        Engine.varibles.add(newWorkVarible1);
        WorkVarible newWorkVarible2 = new WorkVarible(generateNewWorkVaribleName());
        Engine.varibles.add(newWorkVarible2);
        this.ExpandedCommands.add(new Assignment(newWorkVarible1, this.label, this.varible, this));
        for(Varible v : Engine.varibles) {
            if(v.getName().equals(this.variableName)) {
                this.ExpandedCommands.add(new Assignment(newWorkVarible2, "   ", v, this));
                break;
            }
        }
        String newLabel1 = generateNewLabel();
        Engine.labels.add(newLabel1);
        String newLabel2 = generateNewLabel();
        Engine.labels.add(newLabel2);
        String newLabel3 = generateNewLabel();
        Engine.labels.add(newLabel3);
        this.ExpandedCommands.add(new JumpZero(newWorkVarible1,newLabel3,newLabel2,this));
        this.ExpandedCommands.add(new JumpZero(newWorkVarible2,newLabel1,"   ",this));
        this.ExpandedCommands.add(new Decrease(newWorkVarible1,"   ",this));
        this.ExpandedCommands.add(new Decrease(newWorkVarible2,"   ",this));
        this.ExpandedCommands.add(new GotoLabel(newLabel2,this));
        this.ExpandedCommands.add(new JumpZero(newWorkVarible2,this.JEVariableLabel,newLabel3,this));
        for(Varible v : Engine.varibles) {
            if(v instanceof OutputVarible){
                this.ExpandedCommands.add(new Neutral(v,newLabel1,this));
            }
        }
        expandFurther();
    }

    @Override
    public String execute(int expansionLevel) {
        int varValue = this.varible.getValue();
        int checkedValue;
        for(Varible var : Engine.varibles)
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
        Varible newVar = extractVariables(newVarName);
        Engine.varibles.add(newVar);
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
        return "IF " + this.varible.getName() + " = " + this.variableName + " THEN JUMP TO " + JEVariableLabel;
    }
}
