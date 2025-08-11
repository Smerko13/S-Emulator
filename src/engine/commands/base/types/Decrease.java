package engine.commands.base.types;

import engine.commands.base.BaseCommand;

public class Decrease extends BaseCommand {
    public Decrease(){
        super();
        this.commandName = "DECREASE";
        this.cycles = 1;
    }

    public int decrease(int V){
        return V - 1;
    }
}
