package engine.commands;

import engine.Program;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.synthetic.types.Quote;
import schema.SInstruction;

import java.io.Serializable;
import java.util.*;

public abstract class Command implements Serializable, Cloneable {
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
    protected Program associatedProgram;

    public Command(SInstruction instruction, Program program) {
        this.associatedProgram = program;
        this.associatedLabels = new LinkedHashSet<>();
        if (instruction.getSLabel() != null) {
            String label = instruction.getSLabel();
            if(label.length() == 2) {
                label = label + " "; // Ensure label has at least 3 characters
            }
            this.label = label;
            this.associatedProgram.labels.add(label);
            this.associatedLabels.add(label);
        }
        String var = instruction.getSVariable();
        this.variable = extractVariables(var);
        this.associatedVariables = new LinkedHashSet<>();
        this.associatedVariables.add(this.variable);
    }

    public Command(Variable variable, String label, Command parentCommand, Program program) {
        this.associatedProgram = program;
        this.parentCommand = parentCommand;
        if(label.length() == 2) {
            label = label + " "; // Ensure label has at least 3 characters
        }
        this.label = label;
        this.associatedLabels = new LinkedHashSet<>();
        this.associatedLabels.add(label);
        this.associatedProgram.labels.add(label);
        this.associatedVariables = new LinkedHashSet<>();
        if(variable != null) {
            this.variable = extractVariables(variable.getName());
        } else {
            this.variable = null; // Handle case where variable is null
        }
        this.associatedVariables.add(this.variable);
    }

    public Command(Command cmd, Quote quote , String label, Variable outputVar, Program program) {
        this.associatedProgram = program;
        this.associatedVariables = new LinkedHashSet<>();
        if(outputVar == null) {
            this.variable = new WorkVariable(generateNewWorkVariableName());
            this.associatedProgram.getVariables().add(this.variable);
            this.associatedVariables.add(variable);
        } else if (outputVar instanceof  InputVariable) {
            this.variable = new WorkVariable(generateNewWorkVariableName());
            this.variable.setValue(outputVar.getValue());
            this.associatedProgram.getVariables().add(this.variable);
            this.associatedVariables.add(variable);
        } else {
            this.variable = outputVar;
            this.associatedVariables.add(outputVar);
        }
        this.label = cmd.label;
        this.cycles = cmd.cycles;
        this.commandType = cmd.commandType;
        this.isExpandable = cmd.isExpandable;
        this.commandName = cmd.commandName;
        this.levelOfExpansion = cmd.levelOfExpansion;
        this.parentCommand = quote;
        this.associatedLabels = new LinkedHashSet<>();
        this.associatedLabels.add(label);
        this.associatedProgram.labels.add(label);
    }

    protected Variable extractVariables(String var) {
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
        return this.canonicalInGlobalScope(variable);
    }

    private Variable canonicalInGlobalScope(Variable variable) {
        for(Variable existingVar : this.associatedProgram.getVariables()) {
            if (existingVar.getName().equals(variable.getName())) {
                return existingVar; // Return the existing variable if found
            }
        }
        this.associatedProgram.getVariables().add(variable); // Add the new variable to the global scope
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


    public abstract int getExpansionDepth();


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
        if (commandType == 'B') {
            return "Basic";
        } else if (commandType == 'S') {
            return "Synthetic";
        } else {
            return "UNKNOWN";
        }
    }

    public abstract Set<Variable> getAllVariables();

    public Collection<String> getUsedVariableNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Variable v : associatedVariables) {
            if (v != null && v.getName() != null) {
                names.add(v.getName());
            }
        }
        return names;
    }

    public String getCommandName() {
        return commandName;
    }

    public String generateNewWorkVariableName() {
        Set<String> existingNames = new HashSet<>();
        for (Variable var : this.associatedProgram.getVariables()) {
            existingNames.add(var.getName());
        }
        int workArgIndex = 1;
        String currentWorkVarName;
        do {
            currentWorkVarName = "z" + workArgIndex++;
        } while (existingNames.contains(currentWorkVarName));
        return currentWorkVarName;
    }

    public Object getVar() {
        return this.variable;
    }

    @Override
    public Command clone() {
        try {
            Command cloned = (Command) super.clone();
            // Deep copy mutable fields
            cloned.associatedLabels = new LinkedHashSet<>(this.associatedLabels);
            cloned.associatedVariables = new LinkedHashSet<>(this.associatedVariables);
            // If variable is mutable, clone it as well
            if (this.variable != null) {
                cloned.variable = this.variable.clone();
            }
            // Note: associatedProgram is not cloned (shared reference)
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

    public abstract Collection<String> getAssociatedLabels();

    public void replaceVariable(Variable variable, WorkVariable v) {
        if (this.variable.getName().equals(variable.getName())) {
            this.variable = v;
            for (Variable var : this.associatedVariables) {
                if (var.getName().equals(variable.getName())) {
                    this.associatedVariables.remove(var);
                    this.associatedVariables.add(v);
                    break;
                }
            }
        }
    }

    public abstract void replaceLabel(String lbl, String newLabel);

    public void setParent(Command parent) {
        this.parentCommand = parent;
    }

    public void setAssociatedEngine(Program program) {
        this.associatedProgram = program;
    }

    public int getId() {
        return id;
    }
}
