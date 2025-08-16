package engine.commands.synthetic.types;

import engine.commands.base.types.Decrease;
import engine.commands.Command;
import engine.commands.base.types.JumpNotZero;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.util.ArrayList;
import java.util.List;

public class ZeroVariable extends SyntheticCommand {

    public ZeroVariable(SInstruction instruction) {
        super(instruction);
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.levelOfExpansion = 1;
    }

    public void initializeExpandedCommands() {
    }

    @Override
    public String toString() {
        return varible.getName() + " <- 0";
    }
}
