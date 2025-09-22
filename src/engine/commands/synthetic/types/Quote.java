package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.*;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.util.*;
import java.util.stream.Collectors;

public class Quote extends SyntheticCommand {
    String functionName;
    List<String> functionArguments;
    LinkedHashMap<Variable, Boolean> functionArgumentsVariables;


    public Quote(Variable assignedVariable, String functionName, List<String> functionArguments, String label, Command parentCommand, Engine engine) {
        super(assignedVariable, label, parentCommand, engine);
        this.commandName = "QUOTE";
        this.functionName = functionName;
        this.functionArguments = functionArguments;
        this.functionArgumentsVariables = new LinkedHashMap<>();
        initializeFunctionArgumentVariables();
        this.associatedVariables.addAll(functionArgumentsVariables.keySet());
        this.cycles = 5 + calculateSubFunctionCycles();
        this.levelOfExpansion = calculateSubFunctionExpansionLevel() + 1;
    }

    public Quote(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "QUOTE";
        this.functionName = findCorrectFunctionName(instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue());
        String args = instruction.getSInstructionArguments().getSInstructionArgument().get(1).getValue();
        this.functionArguments = (args.isEmpty() ? new ArrayList<>() : Arrays.asList(args.split(",")));
        this.functionArgumentsVariables = new LinkedHashMap<>();
        initializeFunctionArgumentVariables();
        this.associatedVariables.addAll(functionArgumentsVariables.keySet());
        this.cycles = 5 + calculateSubFunctionCycles();
        this.levelOfExpansion = calculateSubFunctionExpansionLevel() + 1;
    }

    private void initializeFunctionArgumentVariables() {
        for(String arg : functionArguments) {
            boolean found = false;
            for(Variable var : this.associatedEngine.getVariables()) {
                if (var.getName().equals(arg)) {
                    functionArgumentsVariables.put(var, true);
                    found = true;
                    break;
                }
            }
            if(!found) {
                Variable newVar = getVariable(arg);
                this.associatedEngine.getVariables().add(newVar);
                functionArgumentsVariables.put(newVar, true);
            }
        }
    }

    private Variable getVariable(String arg) {
        Variable newVar = null;
        if(arg.charAt(0) == 'z') { newVar = new WorkVariable(arg);}
        else if(arg.charAt(0) == 'x') { newVar = new InputVariable(arg);}
        else if (arg.charAt(0) == 'y') { 
            for(Variable var : this.associatedEngine.getVariables()) {
                if(var instanceof InputVariable && var.getName().equals(arg)) {
                    newVar = var;
                }
            }
        }
        return newVar;
    }

    private int calculateSubFunctionExpansionLevel() {
        int maxLevel = 2;
        for(Engine e : this.associatedEngine.subFunctions) {
            String userString = e.getUserString();
            if(userString.equals(functionName)) {
                for(Command cmd : e.getCommands()) {
                    if(cmd.getExpansionDepth() > maxLevel) {
                        maxLevel = cmd.getExpansionDepth();
                    }
                }
            }
        }
        return maxLevel;
    }

    private int calculateSubFunctionCycles() {
        for(Engine e : this.associatedEngine.subFunctions) {
            String userString = e.getUserString();
            if(userString.equals(functionName)) {
                return e.getTotalCycles();
            }
        }
        return 0;
    }

    @Override
    public void initializeExpandedCommands() {
        String newOutputVarName = null;

        if(!this.label.trim().isEmpty()) {
            this.ExpandedCommands.add(new Neutral(this.associatedEngine.getOutputVar(),this.label,this, this.associatedEngine));
        }

        for(Engine e : this.associatedEngine.subFunctions) {
            String userString = e.getUserString();
            boolean exitLabelRequired = false;
            String exitLabel = null;
            if(userString.equals(functionName)) {
                Engine clonedSubFunction = e.clone();
                List<Command> subFunctionCommands = clonedSubFunction.getCommands();

                for (String lbl : clonedSubFunction.labels) {
                    if (lbl.equals("EXIT")) continue;
                    if (!this.associatedEngine.labels.contains(lbl)) {
                        this.associatedEngine.labels.add(lbl);
                    } else {
                        String newLabel = generateNewLabel();
                        for (Command cmd : subFunctionCommands) {
                            cmd.replaceLabel(lbl, newLabel);
                        }
                        this.associatedEngine.labels.add(newLabel);
                    }
                }

                for(Variable v : clonedSubFunction.getVariables()) {
                    if(v instanceof WorkVariable) {
                        String newWorkVarName = generateNewWorkVariableName();
                        WorkVariable newWorkVar = new WorkVariable(newWorkVarName);
                        this.associatedEngine.getVariables().add(newWorkVar);
                        for(Command cmd : subFunctionCommands) {
                            cmd.replaceVariable(v, newWorkVar);
                        }
                    } else if (v instanceof OutputVariable) {
                        newOutputVarName = generateNewWorkVariableName();
                        WorkVariable newWorkVar = new WorkVariable(newOutputVarName);
                        this.associatedEngine.getVariables().add(newWorkVar);
                        for(Command cmd : subFunctionCommands) {
                            cmd.replaceVariable(v, newWorkVar);
                        }
                    } else if (v instanceof InputVariable) {
                        String newWorkVarName = generateNewWorkVariableName();
                        WorkVariable newWorkVar = new WorkVariable(newWorkVarName);
                        this.associatedEngine.getVariables().add(newWorkVar);
                        for(Command cmd : subFunctionCommands) {
                            cmd.replaceVariable(v, newWorkVar);
                        }
                        for(Variable funcArgVar : functionArgumentsVariables.keySet()) {
                            if(functionArgumentsVariables.get(funcArgVar) == true) {
                                this.ExpandedCommands.add(new Assignment(newWorkVar,"   ", funcArgVar, this, this.associatedEngine));
                                functionArgumentsVariables.put(funcArgVar, false);
                                break;
                            }
                        }
                    }
                }

                for (Command cmd : subFunctionCommands) {// might cause some  (last two line)
                    String targetLabel = cmd.getTargetLabel();
                    if (targetLabel != null && targetLabel.equals("EXIT")){
                        exitLabel = this.generateNewLabel()+"END";
                        this.associatedEngine.labels.add(exitLabel);
                        cmd.replaceLabel("EXIT", exitLabel);
                        exitLabelRequired = true;
                    }
                    cmd.setParent(this);
                    cmd.setAssociatedEngine(this.associatedEngine);
                }

                this.ExpandedCommands.addAll(subFunctionCommands);

                for(Variable v: this.associatedEngine.getVariables()) {
                    if(v.getName().equals(newOutputVarName)) {
                        if(!exitLabelRequired) {
                            this.ExpandedCommands.add(new Assignment(this.variable, "   ", v, this, this.associatedEngine));
                        } else {
                            this.ExpandedCommands.add(new Assignment(this.variable, exitLabel, v, this, this.associatedEngine));
                        }
                        break;
                    }
                }
            }
        }
        expandFurther();
    }


    @Override
    public String execute() {
        List<Variable> variables = new ArrayList<>();
        for(String arg : functionArguments) {
            for (Variable v : this.associatedEngine.getVariables()) {
                if(v.getName().equals(arg)) {
                    variables.add(v);
                }
            }
        }
        Set<Variable> snapshot = this.associatedEngine.getVariables().stream()
                .map(Variable::clone)
                .collect(Collectors.toSet());
        for(Engine e : this.associatedEngine.subFunctions) {
            String userString = e.getUserString();
            if(userString.equals(functionName)) {
                int returnValue = e.executeFunction(variables);
                setBackOriginalVariables(snapshot);
                this.variable.setValue(returnValue);
                return null;
            }
        }
        return null;
    }

    private void setBackOriginalVariables(Set<Variable> snapshot) {
        for(Variable v : snapshot) {
            for(Variable originalVar : this.associatedEngine.getVariables()) {
                if(v.getName().equals(originalVar.getName())) {
                    originalVar.setValue(v.getValue());
                }
            }
        }
    }

    @Override
    public boolean isValid() {//need to fix
        return true;
    }

    @Override
    public String getTargetLabel() {
        return "";
    }

    @Override
    public Set<Variable> getAllVariables() {
        Set<Variable> vars = new HashSet<>(functionArgumentsVariables.keySet());
        vars.add(this.variable);
        return vars;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(functionArguments.isEmpty()) {
            return this.variable.getName() + " <- (" + functionName + ")";
        } else if (functionArguments.size() == 1) {
            return this.variable.getName() + " <- (" + functionName + "," + functionArguments.getFirst() + ")";
        } else {
            for (String arg : functionArguments) {
                sb.append(arg).append(", ");
            }
            return this.variable.getName() + " <- (" + functionName + "," + sb + ")";
        }
    }

    private String findCorrectFunctionName(String functionName) {
        for(Engine e : this.associatedEngine.subFunctions) {
            if(e.getCurrentProgramName().equals(functionName)) {
                return e.getUserString();
            }
        }
        return null;
    }

    @Override
    public Quote clone() {
        return (Quote) super.clone();
    }

    @Override
    public Collection<String> getAssociatedLabels() {
        return Collections.singleton(this.label);
    }

    @Override
    public void replaceVariable(Variable variable, WorkVariable v) {
        // Safely update associatedVariables
        Iterator<Variable> it = this.associatedVariables.iterator();
        while (it.hasNext()) {
            Variable var = it.next();
            if (var.equals(variable)) {
                it.remove();
                this.associatedVariables.add(v);
                break;
            }
        }
        // Update functionArgumentsVariables
        if (this.functionArgumentsVariables.containsKey(variable)) {
            Boolean value = this.functionArgumentsVariables.remove(variable);
            this.functionArgumentsVariables.put(v, value);
        }
        // Update functionArguments if needed
        for (int i = 0; i < this.functionArguments.size(); i++) {
            if (this.functionArguments.get(i).equals(variable.getName())) {
                this.functionArguments.set(i, v.getName());
            }
        }
        // Update main variable reference
        if (this.variable.equals(variable)) {
            this.variable = v;
        }
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {
        if (this.label.equals(lbl)) {
            this.label = newLabel;
        }
        this.associatedLabels.remove(lbl);
        this.associatedLabels.add(newLabel);
    }
}
