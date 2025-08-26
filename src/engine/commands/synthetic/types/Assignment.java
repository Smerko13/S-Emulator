package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.Increase;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class Assignment extends SyntheticCommand {
    protected Variable assignedVariable;

    public Assignment(SInstruction instruction) {
        super(instruction);
        this.commandName = "ASSIGNMENT";
        this.cycles = 4;
        this.levelOfExpansion = 2;
        String assignedVar = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.assignedVariable = extractVariables(assignedVar);
        this.associatedVariables.add(assignedVariable);
    }

    public Assignment(WorkVariable newWorkVariable, String label, Variable assignedVariable, Command parentCommand) {
        super(newWorkVariable, label, parentCommand);
        this.commandName = "ASSIGNMENT";
        this.cycles = 4;
        this.levelOfExpansion = 2;
        this.assignedVariable = assignedVariable;
        this.associatedVariables.add(assignedVariable);
    }

    @Override
    public void initializeExpandedCommands() {
        String newLabel1 = generateNewLabel();
        Engine.labels.add(newLabel1);
        String newLabel2 = generateNewLabel();
        Engine.labels.add(newLabel2);
        String newLabel3 = generateNewLabel();
        Engine.labels.add(newLabel3);
        this.ExpandedCommands.add(new ZeroVariable(variable,this.label,this));
        this.ExpandedCommands.add(new JumpNotZero(assignedVariable, newLabel1,"   ", this));
        this.ExpandedCommands.add(new GotoLabel(newLabel3,this));
        this.ExpandedCommands.add(new Decrease(assignedVariable,newLabel1, this));
        WorkVariable newWorkVariable = new WorkVariable(generateNewWorkVariableName());
        Engine.variables.add(newWorkVariable);
        this.ExpandedCommands.add(new Increase(newWorkVariable,"   ",this));
        this.ExpandedCommands.add(new JumpNotZero(assignedVariable, newLabel1, "   ", this));
        this.ExpandedCommands.add(new Decrease(newWorkVariable,newLabel2, this));
        this.ExpandedCommands.add(new Increase(variable,"   ", this));
        this.ExpandedCommands.add(new Increase(assignedVariable,"   ", this));
        this.ExpandedCommands.add(new JumpNotZero(newWorkVariable, newLabel2, "   ", this));
        this.ExpandedCommands.add(new Neutral(variable,newLabel3, this));

        expandFurther();
    }

    @Override
    public String execute() {
        // Assign the value of the assigned variable to the variable
        if (assignedVariable != null) {
            variable.setValue(assignedVariable.getValue());
        }
        return null;
    }

    @Override
    public boolean isValid() {
        if( assignedVariable == null || variable == null) {
            return false;
        } else return assignedVariable.getValue() >= 0 && variable.getValue() >= 0;
    }

    @Override
    public String getTargetLabel() {
        return null;
    }

    @Override
    public String toString() {
        return variable.getName() + " <- " + assignedVariable.getName();
    }
}
