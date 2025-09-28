package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.util.*;

public class Quote extends SyntheticCommand implements Cloneable {
    String functionName;
    String userString;
    String functionArguments;
    List<String> argumentList;

    public Quote(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "QUOTE";
        String name = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        for (Engine e : this.associatedEngine.subFunctions) {
            if (e.getCurrentProgramName().equals(name)) {
                this.userString = e.getUserString();
                this.functionName = e.getCurrentProgramName();
                break;
            }
        }
        if (this.functionName == null) {
            throw new IllegalArgumentException("Function name not found for Quote command: " + name);
        }
        this.functionArguments = instruction.getSInstructionArguments().getSInstructionArgument().get(1).getValue();
        this.argumentList = initializeArgumentList(this.functionArguments);
        initializeAssociatedVariables();
        this.cycles = 5 + calculateSubFunctionCycles(this.functionName);
    }

    private int calculateSubFunctionCycles(String functionName) {
        for (Engine e : this.associatedEngine.subFunctions) {
            if (e.getCurrentProgramName().equals(functionName)) {
                return e.getTotalCycles();
            }
        }
        return 0;
    }

    public Quote(Variable assignedVariable, String functionName, List<String> functionArguments, String label, Command parentCommand, Engine engine) {
        super(assignedVariable, label, parentCommand, engine);
        this.commandName = "QUOTE";
        this.functionName = functionName; // fix needed
        for(Engine e : this.associatedEngine.subFunctions) {
            if(e.getCurrentProgramName().equals(functionName)) {
                this.userString = e.getUserString();
                break;
            }
        }
        this.argumentList = functionArguments;
        this.functionArguments = String.join(",", functionArguments);
        initializeAssociatedVariables();
        this.cycles = 5 + calculateSubFunctionCycles(this.functionName);
    }

    private void initializeAssociatedVariables() {
        List<String> tempList = new ArrayList<>(List.of(this.functionArguments.split(",")));
        if (functionArguments.isEmpty()) {
            return;
        }
        tempList.removeIf(s -> s.charAt(0) == '('); //remove function calls
        for (String arg : tempList) {
            if (arg.startsWith("x") || arg.startsWith("z") || arg.startsWith("y")) {
                int index = 0;
                for (char c : arg.toCharArray()) {
                    if (c == 'x' || c == 'y' || c == 'z' || Character.isDigit(c)) {
                        index++;
                    } else {
                        break;
                    }
                }
                String cleanedArg = arg.substring(0, index);
                boolean found = false;
                for (Variable v : this.associatedEngine.getVariables()) {
                    if (v.getName().equals(cleanedArg)) {
                        this.associatedVariables.add(v);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Variable var = new InputVariable(cleanedArg);
                    this.associatedEngine.getVariables().add(var);
                    this.associatedVariables.add(var);
                }
            }
        }
    }

    private List<String> initializeArgumentList(String functionArguments) {
        List<String> returnList = new ArrayList<>();
        int i = 0;
        if (functionArguments.isEmpty()) {
            return returnList;
        } else {
            StringBuilder sb = new StringBuilder();
            for (char c : functionArguments.toCharArray()) {
                if (c == '(') {
                    i++;
                }
                if (c == ')') {
                    i--;
                }
                if (c == ',' && i == 0) {
                    returnList.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            returnList.add(sb.toString());
            return returnList;
        }
    }

    @Override
    public void initializeExpandedCommands() {
        String newOutputVarName = null;

        if(!this.label.trim().isEmpty()) {
            this.ExpandedCommands.add(new Neutral(this.associatedEngine.getOutputVar(),this.label,this, this.associatedEngine));
        }

        for(Engine e : this.associatedEngine.subFunctions) {
            String SubFunctionName = e.getCurrentProgramName();
            boolean exitLabelRequired = false;
            String exitLabel = null;
            if(SubFunctionName.equals(functionName)) {
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

                // Java
                int index = 0;
                for (Variable v : clonedSubFunction.getVariables()) {
                    if (v instanceof InputVariable) {
                        // Find the corresponding argument by index
                        if (index >= this.argumentList.size()) break;
                        String arg = this.argumentList.get(index++);
                        String newWorkVarName = generateNewWorkVariableName();
                        WorkVariable newWorkVar = new WorkVariable(newWorkVarName);
                        this.associatedEngine.getVariables().add(newWorkVar);

                        // Replace the input variable in the subfunction with the new work variable
                        for (Command cmd : subFunctionCommands) {
                            cmd.replaceVariable(v, newWorkVar);
                        }

                        // Assign the value to the new work variable
                        if (arg.charAt(0) == 'x' || arg.charAt(0) == 'y' || arg.charAt(0) == 'z') {
                            // Find the variable in the parent engine by name
                            for (Variable var : this.associatedEngine.getVariables()) {
                                if (var.getName().equals(arg)) {
                                    this.ExpandedCommands.add(new Assignment(newWorkVar, "   ", var, this, this.associatedEngine));
                                    break;
                                }
                            }
                        } else if (arg.charAt(0) == '(') {
                            // Nested function call: evaluate and assign result
                            String functionName = arg.substring(1, arg.indexOf(',') == -1 ? arg.length() - 1 : arg.indexOf(','));
                            String functionArguments = arg.indexOf(',') == -1 ? "" : arg.substring(arg.indexOf(',') + 1, arg.length() - 1);
                            List<String> subArgumentList = initializeArgumentList(functionArguments);
                            this.ExpandedCommands.add(new Quote(newWorkVar, functionName, subArgumentList, "   ", this, this.associatedEngine));
                        } else {
                            throw new IllegalArgumentException("Invalid argument passed in Quote: " + arg);
                        }
                    }
                    // ... handle WorkVariable and OutputVariable as before ...
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

    // Java
    @Override
    public String execute() {
        int result = 0;
        Set<Variable> variablesSnapshot = takeValueSnapshot(this.associatedEngine.variables);
        for (Engine e : this.associatedEngine.subFunctions) {
            if (e.getCurrentProgramName().equals(functionName)) {
                List<Variable> varsToPass = new ArrayList<>();
                List<Variable> subInputVars = new ArrayList<>();
                for (Variable v : e.getVariables()) {
                    if (v instanceof InputVariable) {
                        subInputVars.add(v);
                    }
                }
                for (int i = 0; i < subInputVars.size(); i++) {
                    if(i >= this.argumentList.size()) break;
                    String arg = argumentList.get(i);
                    if (arg.charAt(0) == 'x' || arg.charAt(0) == 'y' || arg.charAt(0) == 'z') {
                        for (Variable v : this.associatedEngine.getVariables()) {
                            if (v.getName().equals(arg)) {
                                varsToPass.add(v);
                                break;
                            }
                        }
                    } else if (arg.charAt(0) == '(') {
                        varsToPass.add(handleFunctionCall(arg));
                    } else {
                        throw new IllegalArgumentException("Invalid argument passed in Quote: " + arg);
                    }
                }
                result = e.executeFunction(varsToPass);
                break;
            }
        }
        for (Variable var : this.associatedEngine.variables) {
            for (Variable snapshotVar : variablesSnapshot) {
                if (var.getName().equals(snapshotVar.getName())) {
                    var.setValue(snapshotVar.getValue());
                    break;
                }
            }
        }
        this.variable.setValue(result);
        return null;
    }

    private Set<Variable> takeValueSnapshot(Set<Variable> variables) {
        Set<Variable> snapshot = new HashSet<>();
        for (Variable var : variables) {
            Variable varCopy;
            if (var instanceof InputVariable) {
                varCopy = var.clone();
            } else if (var instanceof OutputVariable) {
                varCopy = var.clone();
            } else if (var instanceof WorkVariable) {
                varCopy = var.clone();
            } else {
                throw new IllegalArgumentException("Unknown variable type: " + var.getClass().getName());
            }
            varCopy.setValue(var.getValue());
            snapshot.add(varCopy);
        }
        return snapshot;
    }


    private Variable handleFunctionCall(String arg) {
        //handle function calls inside arguments
        Variable var = null;
        List<Variable> subVarsToPass;
        for (Engine subE : this.associatedEngine.subFunctions) {
            String name = arg.substring(1, arg.indexOf(',') == -1 ? arg.length() - 1 : arg.indexOf(','));
            if (subE.getCurrentProgramName().equals(name)) {
                subVarsToPass = new ArrayList<>();
                String subFunctionArguments = arg.indexOf(',') == -1 ? "" : arg.substring(arg.indexOf(',') + 1, arg.length() - 1);
                List<String> subArgumentList = initializeArgumentList(subFunctionArguments);
                for (String subArg : subArgumentList) {
                    if (subArg.charAt(0) == 'x' || subArg.charAt(0) == 'y' || subArg.charAt(0) == 'z') {
                        for (Variable v : this.associatedEngine.getVariables()) {
                            if (v.getName().equals(subArg)) {
                                subVarsToPass.add(v);
                                break;
                            }
                        }
                    } else if (subArg.charAt(0) == '(') {
                        subVarsToPass.add(handleFunctionCall(subArg));

                    } else {
                        throw new IllegalArgumentException("Invalid argument passed in Quote: " + subArg);
                    }
                }
                subE.setVariables(new ArrayList<>(this.associatedEngine.getVariables())); //try to delete
                int resultOfSubFunction = subE.executeFunction(subVarsToPass);
                var = new WorkVariable("temp");
                var.setValue(resultOfSubFunction);
                break;
            }
        }
        return var;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String getTargetLabel() {
        return "";
    }

    @Override
    public Set<Variable> getAllVariables() {
        return Set.copyOf(this.associatedVariables);
    }

    @Override
    public Collection<String> getAssociatedLabels() {
        return List.of(this.label);
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {
        if (this.label.equals(lbl)) {
            this.label = newLabel;
            this.associatedLabels.remove(lbl);
            this.associatedLabels.add(newLabel);
        }
    }

    @Override
    public String toString() {
        if(userString == null) {
            if (functionArguments.isEmpty()) {
                return variable.getName() + " <- (" + this.functionName + ")";
            } else {
                return variable.getName() + " <- (" + this.functionName + "," + functionArguments + ")";
            }
        }
        if (functionArguments.isEmpty()) {
            return variable.getName() + " <- (" + userString + ")";
        } else {
            return variable.getName() + " <- (" + userString + "," + functionArguments + ")";
        }
    }

    @Override
    public int getExpansionDepth() {
        int maxDepth = 1; // Start with 1 for the current command
        for (Command cmd : this.getExpandedCommands()) {
            if (cmd.getExpansionDepth() > maxDepth) {
                maxDepth = cmd.getExpansionDepth() + 1; // Add 1 for the current command
            }
        }
        return maxDepth;
    }

    // Java
    @Override
    public Quote clone() {
        Quote cloned = (Quote) super.clone();
        // Deep copy mutable fields
        cloned.argumentList = new ArrayList<>(this.argumentList);
        // Strings are immutable, so direct assignment is fine
        cloned.functionName = this.functionName;
        cloned.functionArguments = this.functionArguments;
        // Associated variables and other fields are handled by SyntheticCommand's clone
        return cloned;
    }
}
