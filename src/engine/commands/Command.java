package engine.commands;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import schema.SInstruction;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Command {
    protected String label = "   "; // Default label
    protected int cycles;
    protected char commandType;
    protected Boolean isExpandable;
    protected String commandName;
    protected int levelOfExpansion;
    protected Variable variable;
    protected Command parentCommand = null;
    protected int id;
    protected Set<String> associatedLabels;

    public Command(SInstruction instruction) {
        this.associatedLabels = new LinkedHashSet<>();
        if (instruction.getSLabel() != null) {
            String label = instruction.getSLabel();
            if(label.length() == 2) {
                label = label + " "; // Ensure label has at least 3 characters
            }
            this.label = label;
            this.associatedLabels.add(label);
        }
        String var = instruction.getSVariable();
        this.variable = extractVariables(var);
    }

    public Command(Variable variable, String label, Command parentCommand) {
        this.parentCommand = parentCommand;
        if(label.length() == 2) {
            label = label + " "; // Ensure label has at least 3 characters
        }
        this.label = label;
        this.associatedLabels = new LinkedHashSet<>();
        this.associatedLabels.add(label);
        if(variable != null) {
            this.variable = extractVariables(variable.getName());
        } else {
            this.variable = null; // Handle case where variable is null
        }
    }

    protected static Variable extractVariables(String var) {
        //need to check if the variable already exists in the global scope
        Variable variable = null;
        if(var.charAt(0) == 'x') {
            variable = new InputVariable(var);
        }
        else if(var.charAt(0) == 'z') {
            variable = new WorkVariable(var);
        }
        else if (var.charAt(0) == 'y') {
            variable = new OutputVariable();
        }
        return canonicalInGlobalScope(variable);
    }

    private static Variable canonicalInGlobalScope(Variable variable) {
        for(Variable existingVar : Engine.variables) {
            if (existingVar.getName().equals(variable.getName())) {
                return existingVar; // Return the existing variable if found
            }
        }
        Engine.variables.add(variable); // Add the new variable to the global scope
        return variable;
    }

    public String getLabel() {
        return label;
    }

    public String getCommandRepresentation() {
        return String.format("#%d (%c) [ %s ] %s (%d)",
                             id,
                             commandType,
                             label,
                             this,
                             cycles);
    }


    public int getExpansionDepth() {
        return levelOfExpansion;
    }

    public Variable getVariable() {
        return variable;
    }

    public abstract String execute();

    public int getCycles() {
        return cycles;
    }

    public Command getParentCommand() {
        return parentCommand;
    }

    public void setID(int i) {
        this.id = i;
    }

    public String getLabels() {
        StringBuilder labels = new StringBuilder();
        for(String label : associatedLabels) {
            labels.append(label).append(" ");
        }
        return labels.toString().trim();
    }
}
