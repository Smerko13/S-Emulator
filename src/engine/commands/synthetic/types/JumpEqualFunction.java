package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.util.*;
import java.util.stream.Collectors;

public class JumpEqualFunction extends SyntheticCommand {
    private String JEFunctionLabel;
    private String functionName;
    private List<String> functionArguments;

    public JumpEqualFunction(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "JUMP_EQUAL_FUNCTION";
        JEFunctionLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.associatedLabels.add(JEFunctionLabel);
        this.associatedEngine.labels.add(JEFunctionLabel);
        this.isJumpCommand = true;
        this.functionName = findCorrectFunctionName(instruction.getSInstructionArguments().getSInstructionArgument().get(1).getValue());
        String args = instruction.getSInstructionArguments().getSInstructionArgument().get(2).getValue();
        this.functionArguments = (args.isEmpty() ? new ArrayList<>() : Arrays.asList(args.split(",")));
        this.cycles = 6 + calculateSubFunctionCycles();
        this.levelOfExpansion = calculateSubFunctionExpansionLevel() + 1;
    }

    private int calculateSubFunctionExpansionLevel() {
        for(Engine e : this.associatedEngine.subFunctions) {
            String userString = e.getUserString();
            if(userString.equals(functionName)) {
                return e.getMaxExpansionDepth();
            }
        }
        return 0;
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

    }

    @Override
    public String execute() {
        int returnValue = -1;
        List<Variable> variables = new ArrayList<>();
        for(String arg : functionArguments) {
            for (Variable v : this.associatedEngine.getVariables()) {
                if(v.getName().equals(arg)) {
                    variables.add(v);
                }
            }
        }
        Set<Variable> snapshot = this.associatedEngine.getVariables().stream()
                .map(v -> v.clone())
                .collect(Collectors.toSet());
        for(Engine e : this.associatedEngine.subFunctions) {
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
            for(Variable originalVar : this.associatedEngine.getVariables()) {
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
        return Collections.singleton(this.variable);
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
        for(Engine e : this.associatedEngine.subFunctions) {
            if(e.getCurrentProgramName().equals(functionName)) {
                return e.getUserString();
            }
        }
        return null;
    }
}
