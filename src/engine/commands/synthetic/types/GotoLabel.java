package engine.commands.synthetic.types;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class GotoLabel extends SyntheticCommand {
    private String gototLabel;

    public GotoLabel(SInstruction instruction) {
        super(instruction);
        this.commandName = "GOTO_LABEL";
        this.cycles = 1;
        this.levelOfExpansion = 1;
        gototLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
    }

    @Override
    public void initializeExpandedCommands() {
    }

    @Override
    public String toString() {
        return "GOTO " + gototLabel;
    }
}
