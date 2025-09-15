package engine.commands;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import schema.SInstruction;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Command implements Serializable {
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
    protected Set<Variable> associatedVariables;
    protected boolean isJumpCommand = false;

    public Command(SInstruction instruction) {
        this.associatedLabels = new LinkedHashSet<>();
        if (instruction.getSLabel() != null) {
            String label = instruction.getSLabel();
            if(label.length() == 2) {
                label = label + " "; // Ensure label has at least 3 characters
            }
            this.label = label;
            Engine.labels.add(label);
            this.associatedLabels.add(label);
        }
        String var = instruction.getSVariable();
        this.variable = extractVariables(var);
        this.associatedVariables = new LinkedHashSet<>();
        this.associatedVariables.add(this.variable);
    }

    public Command(Variable variable, String label, Command parentCommand) {
        this.parentCommand = parentCommand;
        if(label.length() == 2) {
            label = label + " "; // Ensure label has at least 3 characters
        }
        this.label = label;
        this.associatedLabels = new LinkedHashSet<>();
        this.associatedLabels.add(label);
        Engine.labels.add(label);
        this.associatedVariables = new LinkedHashSet<>();
        if(variable != null) {
            this.variable = extractVariables(variable.getName());
        } else {
            this.variable = null; // Handle case where variable is null
        }
        this.associatedVariables.add(this.variable);
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
        StringBuilder paddedLabel = new StringBuilder(this.label);
        paddedLabel = new StringBuilder(paddedLabel.toString().trim());
        while(paddedLabel.length() < 3) {
            paddedLabel.append(" ");
        }
        return paddedLabel.toString();
    }

    public String getCommandRepresentation() {
        return String.format("%s ",
                             this);
    }


    public int getExpansionDepth() {
        return levelOfExpansion;
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

    public Variable[] getAssociatedVariables() {
        return associatedVariables.toArray(new Variable[0]);
    }

    public abstract boolean isValid();

    public boolean isJumpCommand() {
        return isJumpCommand;
    }

    public abstract String getTargetLabel();

    public String getType() {
        return String.valueOf(commandType);
    }

    public abstract Set<Variable> getAllVariables();

    // src/engine/commands/Command.java


    public Collection<String> getUsedVariableNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Variable v : associatedVariables) {
            if (v != null && v.getName() != null) {
                names.add(v.getName());
            }
        }
        return names;
    }
}
