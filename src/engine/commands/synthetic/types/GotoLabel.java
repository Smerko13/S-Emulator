package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.Increase;
import engine.commands.base.types.JumpNotZero;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class GotoLabel extends SyntheticCommand {
    private final String gototLabel;

    public GotoLabel(SInstruction instruction) {
        super(instruction);
        this.commandName = "GOTO_LABEL";
        this.cycles = 1;
        this.levelOfExpansion = 1;
        gototLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
    }
    
    public GotoLabel(String gototLabel, Command parentCommand) {
        super(null,"   ", parentCommand);
        this.commandName = "GOTO_LABEL";
        this.cycles = 1;
        this.levelOfExpansion = 1;
        this.gototLabel = gototLabel;
    }

    @Override
    public void initializeExpandedCommands() {
        this.ExpandedCommands.add(new Increase(this.variable, this.label,this));
        this.ExpandedCommands.add(new JumpNotZero(this.variable,this.gototLabel, "   ",this));
    }

    @Override
    public String execute(int expansionLevel) {
        int workArgIndex = 1;
        for(Variable variable : Engine.variables) {
            if(variable instanceof WorkVariable ){
                if(((WorkVariable) variable).isForGotoLabel())
                {
                    variable.setValue(variable.getValue()+1);
                    ((WorkVariable) variable).setForGotoLabel(true);
                    return gototLabel;
                }
            }
            if(variable instanceof WorkVariable && variable.getName().charAt(1) == workArgIndex + '0'){
                workArgIndex++;
            }
        }
        // If no work variable found, create a new one
        WorkVariable newWorkVar = new WorkVariable("z" + workArgIndex);
        newWorkVar.setValue(1);
        newWorkVar.setForGotoLabel(true);
        Engine.variables.add(newWorkVar);

        return gototLabel;
    }

    @Override
    public String toString() {
        return "GOTO " + gototLabel;
    }
}
