package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class JumpEqualConstant extends SyntheticCommand {
    private final String JEConstantLabel; // Label for the jump if condition is met
    private final int constantValue; // Assuming a constant value for comparison


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
        Engine.labels.add(newLabel);
        WorkVariable newWorkVariable = new WorkVariable(generateNewWorkVariableName());
        Engine.variables.add(newWorkVariable);
        this.ExpandedCommands.add(new Assignment(newWorkVariable,this.label, this.variable, this));
        for(int i = 0 ; i < this.constantValue;i++) {
            this.ExpandedCommands.add(new JumpZero(newWorkVariable, newLabel, "   ", this));
            this.ExpandedCommands.add(new Decrease(newWorkVariable, "   ", this));
        }
        this.ExpandedCommands.add(new JumpNotZero(newWorkVariable,newLabel, "   ", this));
        this.ExpandedCommands.add(new GotoLabel(this.JEConstantLabel, this));
        Variable var = null;
        for(Variable v : Engine.variables){
            if( v instanceof OutputVariable)
                var = v;
        }
        this.ExpandedCommands.add(new Neutral(var, newLabel, this));

        expandFurther();
    }

    @Override
    public String execute() {
        // Check if the variable's value equals the constant value
        if (variable.getValue() == constantValue) {
            // If equal, return the label for jumping
            return JEConstantLabel;
        }
        // If not equal, do nothing and continue execution
        return null;
    }

    @Override
    public String toString() {
        return "IF " + variable.getName() + " = " + constantValue + " GOTO " + JEConstantLabel;
    }
}
