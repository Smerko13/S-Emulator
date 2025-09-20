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

public class JumpEqualFunction extends SyntheticCommand {
    private String JEFunctionLabel;
    private String functionName;
    private List<String> functionArguments;

    public JumpEqualFunction(SInstruction instruction) {
        super(instruction);
        this.commandName = "JUMP_EQUAL_FUNCTION";
        this.cycles = 6; // need to calculate
        JEFunctionLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.associatedLabels.add(JEFunctionLabel);
        this.isJumpCommand = true;
        this.functionName = findCorrectFunctionName(instruction.getSInstructionArguments().getSInstructionArgument().get(1).getValue());
        String args = instruction.getSInstructionArguments().getSInstructionArgument().get(2).getValue();
        this.functionArguments = (args.isEmpty() ? new ArrayList<>() : Arrays.asList(args.split(",")));
    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String execute() {
        int returnValue = -1;
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
                returnValue = e.executeFunction(variables);
                setBackOriginalVariables(snapshot);
            }
        }

        return (this.variable.getValue() == returnValue) ? JEFunctionLabel : null;
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
    public boolean isValid() { //need to fix
        return true;
    }

    @Override
    public String getTargetLabel() {
        return this.JEFunctionLabel;
    }

    @Override
    public Set<Variable> getAllVariables() {
        return Set.of();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(functionArguments.isEmpty()) {
            return "IF " + this.variable.getName() + " = (" + functionName + ") GOTO " + JEFunctionLabel;
        } else if (functionArguments.size() == 1) {
            return "IF " + this.variable.getName() + " = (" + functionName + "," + functionArguments.getFirst() + ") GOTO " + JEFunctionLabel;
        } else {
            for (String arg : functionArguments) {
                sb.append(arg).append(", ");
            }
            sb.setLength(sb.length() - 2); // Remove the last comma and space
            return "IF " + this.variable.getName() + " = (" + functionName + "," + sb.toString() + ") GOTO " + JEFunctionLabel;
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
