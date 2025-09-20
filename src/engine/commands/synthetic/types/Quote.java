package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Quote extends SyntheticCommand {
    String functionName;
    List<String> functionArguments;


    public Quote(SInstruction instruction) {
        super(instruction);
        this.commandName = "QUOTE";
        this.cycles = 5; // need to calculate
        this.functionName = findCorrectFunctionName(instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue());
        String args = instruction.getSInstructionArguments().getSInstructionArgument().get(1).getValue();
        this.functionArguments = (args.isEmpty() ? new ArrayList<>() : Arrays.asList(args.split(",")));
    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String execute() {
        List<Variable> variables = new ArrayList<>();
        for(String arg : functionArguments) {
            for (Variable v : Engine.variables) {
                if(v.getName().equals(arg)) {
                    variables.add(v);
                }
            }
        }
        Set<Variable> snapshot = Engine.variables.stream()
                .map(v -> v.clone())
                .collect(Collectors.toSet());
        for(Engine e : Engine.subFunctions) {
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
            for(Variable originalVar : Engine.variables) {
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
        return Set.of();
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
        for(Engine e : Engine.subFunctions) {
            if(e.getCurrentProgramName().equals(functionName)) {
                return e.getUserString();
            }
        }
        return null;
    }
}
