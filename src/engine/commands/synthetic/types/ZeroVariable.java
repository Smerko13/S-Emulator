package engine.commands.synthetic.types;

import engine.Engine;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.JumpNotZero;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.util.ArrayList;

public class ZeroVariable extends SyntheticCommand {

    public ZeroVariable(SInstruction instruction) {
        super(instruction);
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.levelOfExpansion = 1;
    }

    public void initializeExpandedCommands() {
        if(this.label == null || this.label.equals("   ")) {
            String allLabels = Engine.labels;
            String newLabel = "L";
            int labelIndex = 1;
            boolean labelExists = true;
            while (labelExists) {
                labelExists = allLabels.contains(newLabel + labelIndex);
                if (labelExists) {
                    labelIndex++;
                } else {
                    newLabel += labelIndex;
                }
            }
            this.ExpandedCommands.add(new Decrease(this.varible,newLabel));
            this.ExpandedCommands.add(new JumpNotZero(this.varible, newLabel));
        } else {
            this.ExpandedCommands.add(new Decrease(this.varible,this.label));
            this.ExpandedCommands.add(new JumpNotZero(this.varible, this.label));
        }

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
