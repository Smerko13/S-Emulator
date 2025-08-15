package engine.commands.base.types;

import engine.arguments.Varible;
import engine.arguments.types.WorkVarible;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Increase extends BaseCommand {

    public Increase(SInstruction instruction) {
        super();
        this.commandName = "INCREASE";
        if(instruction.getSLabel()!=null){this.label = instruction.getSLabel();}
        this.cycles = 1;
        String var = instruction.getSVariable();
        this.varible = extractVariables(var, this.varible);
    }

    public Increase(Varible var, String label) {
        super();
        this.commandName = "INCREASE";
        this.cycles = 1;
        this.label = label;
        this.varible = var;
    }


    public int increase(int V){
        return V + 1;
    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + varible.getName() + " + 1";
    }
}
