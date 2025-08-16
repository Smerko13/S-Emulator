package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Varible;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class JumpEqualVariable extends SyntheticCommand {
    private String JEVariableLabel;
    private String variableName;

    public JumpEqualVariable(SInstruction instruction) {
        super(instruction);
        this.commandName = "JUMP_EQUAL_VARIABLE";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        this.JEVariableLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.variableName = instruction.getSInstructionArguments().getSInstructionArgument().getLast().getValue();

    }

    @Override
    public void initializeExpandedCommands() {

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
