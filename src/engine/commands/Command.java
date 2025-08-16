package engine.commands;

import engine.Engine;
import engine.arguments.Varible;
import engine.arguments.types.InputVarible;
import engine.arguments.types.OutputVarible;
import engine.arguments.types.WorkVarible;
import schema.SInstruction;

public abstract class Command {
    private static int commandIdTracker = 1;
    private int commandId=0;
    protected String label = "  "; // Default label
    protected int cycles;
    protected char commandType;
    protected Boolean isExpandable;
    protected String commandName;
    protected int levelOfExpansion;
    protected Varible varible;

    public Command(SInstruction instruction) {
        this.commandId = commandIdTracker;
        commandIdTracker++;
        if (instruction.getSLabel() != null) {
            this.label = instruction.getSLabel();
        }
        String var = instruction.getSVariable();
        this.varible = extractVariables(var);
    }

    protected static Varible extractVariables(String var) {
        Varible varible = null;
        if(var.charAt(0) == 'x') {
            varible = new InputVarible(var);
            validateVariableExistenceInGlobalScope(varible);
        }
        else if(var.charAt(0) == 'z') {
            varible = new WorkVarible(var);
            validateVariableExistenceInGlobalScope(varible);
        }
        else if (var.charAt(0) == 'y') {
            varible = new OutputVarible();
            validateVariableExistenceInGlobalScope(varible);
        }
        return varible;
    }

    private static void validateVariableExistenceInGlobalScope(Varible varible) {
        boolean exists = false;
        for (Varible arg : Engine.varibles) {
            if (arg.getName().equals(varible.getName())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            Engine.varibles.add(varible);
        }
    }

    public String getLabel() {
        return label;
    }

    public String getCommandRepresentation() {
        return String.format("#%d (%c) [ %s ] %s (%d)",
                             commandId,
                             commandType,
                             label,
                             this,
                             cycles);
    }


    public int getExpansionDepth() {
        return levelOfExpansion;
    }
}
