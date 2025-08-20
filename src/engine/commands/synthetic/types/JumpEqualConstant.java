package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Varible;
import engine.arguments.types.OutputVarible;
import engine.arguments.types.WorkVarible;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class JumpEqualConstant extends SyntheticCommand {
    private String JEConstantLabel; // Label for the jump if condition is met
    private int constantValue; // Assuming a constant value for comparison


    public JumpEqualConstant(SInstruction instruction) {
        super(instruction);
        this.commandName = "JUMP_EQUAL_CONSTANT";
        this.cycles = 2;
        this.levelOfExpansion = 3;
        this.constantValue = Integer.parseInt(instruction.getSInstructionArguments().getSInstructionArgument().getLast().getValue());
        this.JEConstantLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        Engine.labels.add(JEConstantLabel);
    }

    @Override
    public void initializeExpandedCommands() {
        String newLabel = generateNewLabel();
        WorkVarible newWorkVarible = new WorkVarible(generateNewWorkVaribleName());
        this.ExpandedCommands.add(new Assignment(newWorkVarible,this.label, this.varible));
        for(int i = 0 ; i < this.constantValue;i++) {
            this.ExpandedCommands.add(new JumpZero(newWorkVarible, newLabel, "   "));
            this.ExpandedCommands.add(new Decrease(newWorkVarible, "   "));
        }
        this.ExpandedCommands.add(new JumpNotZero(newWorkVarible,newLabel, "   "));
        this.ExpandedCommands.add(new GotoLabel(this.JEConstantLabel));
        Varible var = null;
        for(Varible v : Engine.varibles){
            if( v instanceof OutputVarible)
                var = v;
        }
        this.ExpandedCommands.add(new Neutral(var, newLabel));
    }

    @Override
    public String execute(int expansionLevel) {
        // Check if the variable's value equals the constant value
        if (varible.getValue() == constantValue) {
            // If equal, return the label for jumping
            return JEConstantLabel;
        }
        // If not equal, do nothing and continue execution
        return null;
    }

    @Override
    public String toString() {
        return "IF " + varible.getName() + " = " + constantValue + " GOTO " + JEConstantLabel;
    }
}
