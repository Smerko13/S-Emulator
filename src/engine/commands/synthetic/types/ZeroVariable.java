package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
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

    public ZeroVariable(Variable variable, String label, Command parentCommand) {
        super(variable, label, parentCommand);
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.levelOfExpansion = 1;
    }

    public void initializeExpandedCommands() {
        String newLabel = generateNewLabel();
        Engine.labels.add(newLabel);
        if(this.label.equals("   ")) {
            this.ExpandedCommands.add(new Decrease(this.variable,newLabel, this));
        } else {
            this.ExpandedCommands.add(new Neutral(this.variable,this.label, this));
            this.ExpandedCommands.add(new Decrease(this.variable,newLabel, this));
        }
        this.ExpandedCommands.add(new JumpNotZero(this.variable,newLabel,"   ", this));
    }

    @Override
    public String execute() {
        this.variable.setValue(0);
        return null;
    }

    @Override
    public String toString() {
        return variable.getName() + " <- 0";
    }
}
