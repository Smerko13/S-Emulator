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
        super();
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        if(instruction.getSLabel()!=null){this.label = instruction.getSLabel();}
        String var = instruction.getSVariable();
        this.varible = extractVariables(var, this.varible);
        this.levelOfExpansion = 1;
        initializeExpandedCommands();
    }

    public void initializeExpandedCommands() {
        Decrease decreaseCommand = new Decrease(this.varible,"L1");
        JumpNotZero jumpNotZeroCommand = new JumpNotZero(this.varible, "L1");
        this.ExpandedCommands.add(decreaseCommand);
        this.ExpandedCommands.add(jumpNotZeroCommand);
    }

    @Override
    public String toString() {
        return varible.getName() + " <- 0";
    }
}
