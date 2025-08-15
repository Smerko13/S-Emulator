package engine.commands.base.types;

import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Neutral extends BaseCommand {
    public Neutral(SInstruction instruction) {
        super();
        this.commandName = "NEUTRAL";
        if(instruction.getSLabel()!=null){this.label = instruction.getSLabel();}
        this.cycles = 0; // Neutral command takes 0 cycle
        String var = instruction.getSVariable();
        this.varible = extractVariables(var, this.varible);
    }

    public int neutral(int V) {
        return V; // Neutral command does not change the value
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + varible.getName();
    }
}
