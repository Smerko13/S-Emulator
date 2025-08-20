package engine.commands.synthetic.types;
import engine.Engine;
import engine.arguments.Varible;
import engine.arguments.types.WorkVarible;
import engine.commands.base.types.Increase;
import engine.commands.base.types.JumpNotZero;
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
        this.ExpandedCommands.add(new Increase(this.varible,this.label));
        this.ExpandedCommands.add(new JumpNotZero(this.varible,this.gototLabel));
    }

    @Override
    public String execute(int expansionLevel) {
        int workArgIndex = 1;
        for(Varible variable : Engine.varibles) {
            if(variable instanceof WorkVarible ){
                if(((WorkVarible) variable).isForGotoLabel())
                {
                    variable.setValue(variable.getValue()+1);
                    ((WorkVarible) variable).setForGotoLabel(true);
                    return gototLabel;
                }
            }
            if(variable instanceof WorkVarible && variable.getName().charAt(1) == workArgIndex + '0'){
                workArgIndex++;
            }
        }
        // If no work variable found, create a new one
        WorkVarible newWorkVar = new WorkVarible("z" + workArgIndex);
        newWorkVar.setValue(1);
        newWorkVar.setForGotoLabel(true);
        Engine.varibles.add(newWorkVar);

        return gototLabel;
    }

    @Override
    public String toString() {
        return "GOTO " + gototLabel;
    }
}
