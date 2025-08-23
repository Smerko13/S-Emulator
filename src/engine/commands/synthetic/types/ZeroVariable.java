package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Varible;
import engine.commands.Command;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;


public class ZeroVariable extends SyntheticCommand {

    public ZeroVariable(SInstruction instruction) {
        super(instruction);
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.levelOfExpansion = 1;
    }

    public ZeroVariable(Varible varible, String label, Command parentCommand) {
        super(varible, label, parentCommand);
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.levelOfExpansion = 1;
    }

    public void initializeExpandedCommands() {
        String newLabel = generateNewLabel();
        Engine.labels.add(newLabel);
        if(this.label.equals("   ")) {
            this.ExpandedCommands.add(new Decrease(this.varible,newLabel, this));
        } else {
            this.ExpandedCommands.add(new Neutral(this.varible,this.label, this));
            this.ExpandedCommands.add(new Decrease(this.varible,newLabel, this));
        }
        this.ExpandedCommands.add(new JumpNotZero(this.varible,newLabel,"   ", this));
    }

    @Override
    public String execute(int expansionLevel) {
        this.varible.setValue(0);
        return null;
    }

    @Override
    public String toString() {
        return varible.getName() + " <- 0";
    }
}
