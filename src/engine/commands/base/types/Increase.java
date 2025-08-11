package engine.commands.base.types;

import engine.commands.base.BaseCommand;

public class Increase extends BaseCommand {

    public Increase() {
        super();
        this.commandName = "INCREASE";
        this.cycles = 1;
    }

    public int increase(int V){
        return V + 1;
    }
}
