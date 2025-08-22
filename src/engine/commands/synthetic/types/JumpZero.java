package engine.commands.synthetic.types;

import com.sun.jdi.connect.Connector;
import engine.Engine;
import engine.arguments.Varible;
import engine.arguments.types.OutputVarible;
import engine.arguments.types.WorkVarible;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class JumpZero extends SyntheticCommand {
    private String JZLabel;

    public JumpZero(SInstruction instruction) {
        super(instruction);
        this.commandName = "JUMP_ZERO";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        JZLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
    }

    public JumpZero(WorkVarible newWorkVarible, String jeConstantLabel, String spaces) {
        super(newWorkVarible,spaces);
        this.commandName = "JUMP_ZERO";
        this.cycles = 2;
        this.levelOfExpansion = 2;
        JZLabel = jeConstantLabel;
    }

    @Override
    public void initializeExpandedCommands() {
        String newLabel = this.generateNewLabel();
        this.ExpandedCommands.add(new JumpNotZero(this.varible, newLabel, this.label));
        this.ExpandedCommands.add(new GotoLabel(this.JZLabel));
        for(Varible v : Engine.varibles) {
            if(v instanceof OutputVarible) {
                this.ExpandedCommands.add(new Neutral(v, newLabel));
            }
        }
        expandFurther();
    }

    @Override
    public String execute(int expansionLevel) {
        // Check if the variable is zero
        if (varible.getValue() == 0) {
            // If zero, return the label for jumping
            return JZLabel;
        }
        // If not zero, do nothing and continue execution
        return null;
    }

    @Override
    public String toString() {
        return "IF " + varible.getName() + " = 0 GOTO " + JZLabel;
    }
}
