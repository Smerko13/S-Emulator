package engine.commands.synthetic.types;

import engine.arguments.Varible;
import engine.arguments.types.WorkVarible;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.Increase;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class Assignment extends SyntheticCommand {
    protected Varible assignedVarible;

    public Assignment(SInstruction instruction) {
        super(instruction);
        this.commandName = "ASSIGNMENT";
        this.cycles = 4;
        this.levelOfExpansion = 2;
        String assignedVar = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.assignedVarible = extractVariables(assignedVar);
    }

    public Assignment(WorkVarible newWorkVarible, String label, Varible assignedVarible) {
        super(newWorkVarible, label);
        this.commandName = "ASSIGNMENT";
        this.cycles = 4;
        this.levelOfExpansion = 2;
        this.assignedVarible = assignedVarible;
    }

    @Override
    public void initializeExpandedCommands() {
        String newLabel1 = generateNewLabel();
        String newLabel2 = generateNewLabel();
        String newLabel3 = generateNewLabel();
        this.ExpandedCommands.add(new ZeroVariable(varible,this.label));
        this.ExpandedCommands.add(new JumpNotZero(assignedVarible, newLabel1,"   "));
        this.ExpandedCommands.add(new GotoLabel(newLabel3));
        this.ExpandedCommands.add(new Decrease(assignedVarible,newLabel1));
        WorkVarible newWorkVarible = new WorkVarible(generateNewWorkVaribleName());
        this.ExpandedCommands.add(new Increase(newWorkVarible,"   "));
        this.ExpandedCommands.add(new JumpNotZero(assignedVarible, newLabel1, "   "));
        this.ExpandedCommands.add(new Decrease(newWorkVarible,newLabel2));
        this.ExpandedCommands.add(new Increase(varible,"   "));
        this.ExpandedCommands.add(new Increase(assignedVarible,"   "));
        this.ExpandedCommands.add(new JumpNotZero(newWorkVarible, newLabel2, "   "));
        this.ExpandedCommands.add(new Neutral(varible,newLabel3));

        expandFurther();
    }

    @Override
    public String execute(int expansionLevel) {
        // Assign the value of the assigned variable to the variable
        if (assignedVarible != null) {
            varible.setValue(assignedVarible.getValue());
        }
        return null;
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + assignedVarible.getName();
    }
}
