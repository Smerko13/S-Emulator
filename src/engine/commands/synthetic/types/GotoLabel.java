package engine.commands.synthetic.types;
import engine.arguments.types.WorkVarible;
import engine.commands.base.types.Increase;
import engine.commands.base.types.JumpNotZero;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class GotoLabel extends SyntheticCommand {
    String targetLabel;

    public GotoLabel(SInstruction instruction) {
        super();
        this.commandName = "GOTO_LABEL";
        this.cycles = 1;
        if(instruction.getSLabel()!=null){this.label = instruction.getSLabel();}
        this.varible = new WorkVarible(instruction.getSVariable());
        targetLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();

    }

    @Override
    public void initializeExpandedCommands() {
        Increase increaseCommand = new Increase(varible, label);
        JumpNotZero jumpNotZeroCommand = new JumpNotZero(varible, targetLabel);
        this.ExpandedCommands.add(increaseCommand);
        this.ExpandedCommands.add(jumpNotZeroCommand);
    }

    @Override
    public String toString() {
        return "GOTO " + targetLabel;
    }
}
