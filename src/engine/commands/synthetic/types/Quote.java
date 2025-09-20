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
          for (String arg : functionArguments) {
            if(arg.charAt(0) == 'x') {
                boolean found = false;
                for (Variable v : this.associatedEngine.getVariables()) {
                    if(v.getName().equals(arg) && v instanceof InputVariable) {
                        functionArgumentsVariables.put(v, true);
                        found = true;
                        break;
                    }
                }
                if(found) continue;
                InputVariable inputVariable = new InputVariable(arg);
                this.associatedEngine.getVariables().add(inputVariable);
                functionArgumentsVariables.put(inputVariable,true);
                associatedVariables.add(inputVariable);
            } else if (arg.charAt(0) == 'y') {
                for (Variable v : this.associatedEngine.getVariables()) {
                    if(v.getName().equals(arg) && v instanceof OutputVariable) {
                        functionArgumentsVariables.put(v,true);
                        break;
                    }
                }
                //something fishy here
            } else {
                boolean found = false;
                for (Variable v : this.associatedEngine.getVariables()) {
                    if(v.getName().equals(arg) && v instanceof WorkVariable) {
                        functionArgumentsVariables.put(v,true);
                        found = true;
                        break;
                    }
                }
                if(found) continue;
                WorkVariable workVariable = new WorkVariable(arg);
                this.associatedEngine.getVariables().add(workVariable);
                functionArgumentsVariables.put(workVariable,true);
                associatedVariables.add(workVariable);
            }
        }
    }


    private int calculateSubFunctionExpansionLevel() {
        for(Engine e : Engine.subFunctions) {
            String userString = e.getUserString();
            if(userString.equals(functionName)) {
                return e.getMaxExpansionDepth();
            }
        }
        return 0;
    }

    private int calculateSubFunctionCycles() {
        for(Engine e : Engine.subFunctions) {
            String userString = e.getUserString();
            if(userString.equals(functionName)) {
                return e.getTotalCycles();
            }
        }
        return 0;
    }

    @Override
    public void initializeExpandedCommands() {
        WorkVariable returnVar = new WorkVariable(generateNewWorkVariableName());
        for(Engine e : Engine.subFunctions) {
            String userString = e.getUserString();
            if(userString.equals(functionName)) {
                for (Command cmd : e.getCommands()) {
                    this.getExpandedCommands().add(cmd);
                }

                for(Variable v : e.getVariables()) {
                    if(v instanceof OutputVariable) {
                        this.getExpandedCommands().add(new Assignment(this.variable, "   ", v,this,this.associatedEngine));
                    }
                }

            }
        }
        expandFurther();
    }

    private String checkLabel(Command cmd, List<Command> commands) {
        if(commands.indexOf(cmd) == 0 && !this.label.trim().isEmpty()) {
            return this.label;
        } else if (!cmd.getLabel().trim().isEmpty()) {
            // need to impelment matchig label algorithem here
            return cmd.getLabel(); // need to change
        } else {
            return "   ";
        }
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
        for(Engine e : Engine.subFunctions) {
            if(e.getCurrentProgramName().equals(functionName)) {
                return e.getUserString();
            }
        }
        return null;
    }
}
