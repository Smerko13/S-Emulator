package engine.commands.base.types;

import engine.Engine;
import engine.arguments.Argument;
import engine.arguments.types.InputArgument;
import engine.arguments.types.OutputArgument;
import engine.arguments.types.WorkArgument;
import engine.commands.base.BaseCommand;
import schema.SInstruction;

public class Increase extends BaseCommand {

    public Increase(SInstruction instruction) {
        super();
        this.commandName = "INCREASE";
        if(instruction.getSLabel()!=null){this.label = instruction.getSLabel();};
        this.cycles = 1;
        String var = instruction.getSVariable();
        if(var.charAt(0) == 'x') {
            InputArgument inputArgument = new InputArgument(var);
            boolean exists = false;
            for(Argument arg : Engine.arguments) {
                if (arg.getName().equals(inputArgument.getName())) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                this.arguments.add(inputArgument);
            } else {
                Engine.arguments.add(inputArgument);
                this.arguments.add(inputArgument);
            }
        }
        else if(var.charAt(0) == 'z') {
            WorkArgument workArgument = new WorkArgument(var);
            boolean exists = false;
            for(Argument arg : Engine.arguments) {
                if (arg.getName().equals(workArgument.getName())) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                this.arguments.add(workArgument);
            } else {
                Engine.arguments.add(workArgument);
                this.arguments.add(workArgument);
            }
        }
        else if (var.charAt(0) == 'y') {
            OutputArgument inputArgument = new OutputArgument();
            boolean exists = false;
            for(Argument arg : Engine.arguments) {
                if (arg.getName().equals(inputArgument.getName())) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                this.arguments.add(inputArgument);
            } else {
                Engine.arguments.add(inputArgument);
                this.arguments.add(inputArgument);
            }
        }
    }

    public int increase(int V){
        return V + 1;
    }

    @Override
    public String toString() {
        return arguments.getFirst().getName() + " <- " + arguments.getFirst().getName() + " + 1";
    }
}
