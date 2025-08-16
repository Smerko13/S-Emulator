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
    protected String label = "   "; // Default label
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
            String label = instruction.getSLabel();
            if(label.length() == 2) {
                label = label + " "; // Ensure label has at least 3 characters
            }
            this.label = label;
        }
        String var = instruction.getSVariable();
        this.varible = extractVariables(var);
    }

    protected static Varible extractVariables(String var) {
        //need to check if the variable already exists in the global scope
        Varible varible = null;
        if(var.charAt(0) == 'x') {
            varible = new InputVarible(var);
        }
        else if(var.charAt(0) == 'z') {
            varible = new WorkVarible(var);
        }
        else if (var.charAt(0) == 'y') {
            varible = new OutputVarible();
        }
        return cannonicalizeInGlobalScope(varible);
    }

    private static Varible cannonicalizeInGlobalScope(Varible varible) {
        for(Varible existingVar : Engine.varibles) {
            if (existingVar.getName().equals(varible.getName())) {
                return existingVar; // Return the existing variable if found
            }
        }
        Engine.varibles.add(varible); // Add the new variable to the global scope
        return varible;
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

    public Object getVarible() {
        return varible;
    }

    public abstract String execute(int expansionLevel);
}
