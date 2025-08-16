package engine.commands.synthetic.types;
import engine.Engine;
import engine.arguments.Varible;
import engine.arguments.types.WorkVarible;
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
    public String execute(int expansionLevel) {
        int workArgIndex = 1;
        for(Varible variable : Engine.varibles) {
            if(variable instanceof WorkVarible ){
                if(((WorkVarible) variable).isForGotoLabel())
                {
                    variable.setValue(variable.getValue()+1);
                    return gototLabel;
                }
            }
            if(variable instanceof WorkVarible && variable.getName().charAt(1) == workArgIndex + '0'){
                workArgIndex++;
            }
        }
        Engine.varibles.add(new WorkVarible("z"+workArgIndex));
        return gototLabel;
    }

    @Override
    public String toString() {
        return "GOTO " + gototLabel;
    }
}
