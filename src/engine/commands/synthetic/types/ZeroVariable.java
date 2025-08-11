package engine.commands.synthetic.types;

import engine.commands.base.types.Decrease;
import engine.commands.Command;
import engine.commands.synthetic.SyntheticCommand;

import java.util.ArrayList;
import java.util.List;

public class ZeroVariable extends SyntheticCommand {
    private List<Command> BaseCommands;
    private Decrease decrease;


    public ZeroVariable(){
        super();
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.BaseCommands = new ArrayList<Command>();
        this.decrease = new Decrease();
    }

    public int zeroVariable(int V) {
        while(V != 0) {
            BaseCommands.add(decrease);
            V = decrease.decrease(V);
        }
        return V; // Return 0 after applying decrease until V is zero
    }
}
