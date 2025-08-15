package engine.commands.base.types;

import engine.arguments.Varible;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Decrease extends BaseCommand {

    public Decrease(SInstruction instruction) {
        super();
        this.commandName = "DECREASE";
        this.cycles = 1;
        if(instruction.getSLabel()!=null){this.label = instruction.getSLabel();}
        String var = instruction.getSVariable();
        this.varible = extractVariables(var, this.varible);
    }

    public Decrease(Varible varible, String l1) {
        super();
        this.commandName = "DECREASE";
        this.cycles = 1;
        this.label = l1;
        this.varible = varible;
    }

    public int decrease(int V){
        if(V > 0) {
            return V - 1;
        } else {
            return V; // No change if V is already 0 or negative
        }
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + varible.getName() + " - 1";
    }
}
