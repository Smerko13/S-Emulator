package engine.commands.base.types;

import engine.commands.base.BaseCommand;

public class Neutral extends BaseCommand {
    public Neutral() {
        super();
        this.commandName = "NEUTRAL";
        this.cycles = 0; // Neutral command takes 1 cycle
    }

    public int neutral(int V) {
        return V; // Neutral command does not change the value
    }
}
