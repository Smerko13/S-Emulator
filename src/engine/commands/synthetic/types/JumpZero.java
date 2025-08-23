package engine.commands.synthetic.types;

import com.sun.jdi.connect.Connector;
import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class JumpZero extends SyntheticCommand {
    private final String JZLabel;

    public JumpZero(SInstruction instruction) {
        super(instruction);
        this.commandName = "JUMP_ZERO";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        JZLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
    }

    public JumpZero(WorkVariable newWorkVariable, String jeConstantLabel, String spaces, Command parentCommand) {
        super(newWorkVariable,spaces, parentCommand);
        this.commandName = "JUMP_ZERO";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        JZLabel = jeConstantLabel;
    }

    @Override
    public void initializeExpandedCommands() {
        String newLabel = this.generateNewLabel();
        this.ExpandedCommands.add(new JumpNotZero(this.variable, newLabel, this.label, this));
        this.ExpandedCommands.add(new GotoLabel(this.JZLabel, this));
        for(Variable v : Engine.variables) {
            if(v instanceof OutputVariable) {
                this.ExpandedCommands.add(new Neutral(v, newLabel, this));
            }
        }
        expandFurther();
    }

    @Override
    public String execute(int expansionLevel) {
        // Check if the variable is zero
        if (variable.getValue() == 0) {
            // If zero, return the label for jumping
            return JZLabel;
        }
        // If not zero, do nothing and continue execution
        return null;
    }

    @Override
    public String toString() {
        return "IF " + variable.getName() + " = 0 GOTO " + JZLabel;
    }
}
