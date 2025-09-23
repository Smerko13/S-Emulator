package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Quote extends SyntheticCommand {
    String functionName;
    String functionArguments;
    List<String> argumentList;

    public Quote(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "QUOTE";
        String name = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        for(Engine e : this.associatedEngine.subFunctions) {
            if(e.getCurrentProgramName().equals(name)) {
                this.functionName = e.getUserString();
                break;
            }
        }
        if (this.functionName == null) {
            throw new IllegalArgumentException("Function name not found for Quote command: " + name);
        }
        this.functionArguments = instruction.getSInstructionArguments().getSInstructionArgument().get(1).getValue();
        this.argumentList = initializeArgumentList(this.functionArguments);
        initializeAssociatedVariables();
    }

    private void initializeAssociatedVariables() {
        List <String> tempList = new ArrayList<>(List.of(this.functionArguments.split(",")));
        if(functionArguments.isEmpty()) {return;}
        tempList.removeIf(s -> s.charAt(0) == '('); //remove function calls
        for (String arg : tempList) {
            if(arg.startsWith("x") || arg.startsWith("z") || arg.startsWith("y")) {
                int index = 0;
                for(char c: arg.toCharArray()) {
                    if(c == 'x' || c == 'y' || c == 'z' || Character.isDigit(c)) {
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

    public Quote(String arg, Engine associatedEngine, Variable tempVar) {
        super(tempVar, "", null, associatedEngine);
        this.commandName = "QUOTE";
        String name = arg.substring(1, arg.indexOf(',') == -1 ? arg.length() - 1 : arg.indexOf(','));
        for(Engine e : this.associatedEngine.subFunctions) {
            if (e.getCurrentProgramName().equals(name)) {
                this.functionName = e.getUserString();
                break;
            }
        }
        this.functionArguments = arg.indexOf(',') == -1 ? "" : arg.substring(arg.indexOf(',') + 1, arg.length() - 1);
        this.argumentList = initializeArgumentList(this.functionArguments);
    }

    private List<String> initializeArgumentList(String functionArguments) {
        List<String> returnList = new ArrayList<String>();
        int i = 0;
        if(functionArguments.isEmpty()) {
            return returnList;
        } else {
            StringBuilder sb = new StringBuilder();
            for(char c : functionArguments.toCharArray()) {
                if (c == '(') { i++; }
                if (c == ')') { i--; }
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

    public Quote(WorkVariable newWorkVariable, String functionName, List<String> functionArguments, String label, JumpEqualFunction jumpEqualFunction, Engine associatedEngine) {
        super(newWorkVariable, label, jumpEqualFunction, associatedEngine);
    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String execute() {
        int result = 0;
        for(Engine e : this.associatedEngine.subFunctions) {
            if(e.getUserString().equals(functionName)) {
                List<Variable> varsToPass = new ArrayList<Variable>();
                for(String arg : argumentList) {
                    if(arg.charAt(0) == 'x' || arg.charAt(0) == 'y' || arg.charAt(0) == 'z') {
                        for (Variable v : this.associatedEngine.getVariables()) {
                            if(v.getName().equals(arg)) {
                                varsToPass.add(v);
                                break;
                            }
                            //Variable var = new WorkVariable(generateNewWorkVariableName());
                            //varsToPass.add(var);
                        }
                    } else if (arg.charAt(0) == '(') {
                        //handle function calls inside arguments
                        for (Engine subE : this.associatedEngine.subFunctions) {
                            String name = arg.substring(1, arg.indexOf(',') == -1 ? arg.length() - 1 : arg.indexOf(','));
                            if (subE.getCurrentProgramName().equals(name)) {
                                List<Variable> subVarsToPass = new ArrayList<Variable>();
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

                                    } else {
                                        throw new IllegalArgumentException("Invalid argument passed in Quote: " + subArg);
                                    }
                                }
                                subE.setVariables(new ArrayList<>(this.associatedEngine.getVariables()));
                                int resultOfSubFunction = subE.executeFunction(subVarsToPass);
                                Variable var = new WorkVariable("temp");
                                var.setValue(resultOfSubFunction);
                                varsToPass.add(var);
                                break;
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid argument passed in Quote: " + arg);
                    }
                }
                result = e.executeFunction(varsToPass);
                break;
            }
        }
        this.variable.setValue(result);
        return null;
    }

    private void handleFunctionCall(String arg) {

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
        return List.of();
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {

    }

    @Override
    public String toString() {
        if(functionArguments.isEmpty()) {
            return variable.getName() + " <- (" + functionName + ")";
        } else {
            return variable.getName() + " <- (" + functionName + "," + functionArguments + ")";
        }
    }
}
