package engine;

import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.*;
import engine.commands.synthetic.SyntheticCommand;
import engine.commands.synthetic.types.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import schema.*;

import java.io.*;
import java.util.*;

public class Engine implements S_Emulator , Serializable {
    private List<Command> commands;
    private String currentProgramName;
    public static Set<Variable> variables;
    public static Set<Variable> extraInputVariables;
    private int cycleSum;
    private Stats stats;
    public static Set<String> labels;
    private int curretDegree = 0;


    public Engine() {
        this.commands = new ArrayList<>();
        variables = new LinkedHashSet<>();
        extraInputVariables = new LinkedHashSet<>();
        this.stats = new Stats();
        labels = new LinkedHashSet<>();
    }

    public void arrangeIDs(int expansionLevel) {
        List<Command> commands = getCommandsAtDesiredLevel(expansionLevel);
        int id = 1;
        for(Command cmd : commands) {
            cmd.setID(id);
            id++;
        }
    }



    public String getCurrentProgramName() {
        return currentProgramName;
    }

    public Boolean readProgramFromXml(String filePath) {
        boolean found = false;
        try {
            File xmlFile = new File(filePath);
            if (xmlFile.exists()) {
                found = true;
                JAXBContext jaxbContext = JAXBContext.newInstance(SProgram.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                SProgram program = (SProgram) jaxbUnmarshaller.unmarshal(xmlFile);
                parseObjectToLocalVariables(program);
                if(!checkIfProgramIsValid()) {
                    return false;
                }
                this.stats.reset();
            }
        } catch (JAXBException e) {
            return false;
        }
        return found;
    }

    private boolean checkIfProgramIsValid() {
        for (Command command : this.commands) {
            if (!command.isValid()) {
                throw new IllegalArgumentException("[ERROR] Invalid command found in the program.");
            }
        }
        if(!checkLabelsValidity()) {
            throw new IllegalArgumentException("[ERROR] Found a target label in one of your commands with no existing target .");
        }
        if(checkForDuplicateLabels()) {
            throw new IllegalArgumentException("[ERROR] Found a label pointing to multiple commands.");
        }
        return true;
    }

    private boolean checkForDuplicateLabels() {
        List<String> labelsList = new ArrayList<>();
        for(Command command : this.commands) {
            if(command.getLabel() != null && !command.getLabel().isBlank()) {
                if(labelsList.contains(command.getLabel().trim())) {
                    return true;
                }
                labelsList.add(command.getLabel().trim());
            }
        }
        return  false;
    }

    private boolean checkLabelsValidity() {
        List<String> labelsList = new ArrayList<>();
        labelsList.add("EXIT");
        for(Command command : this.commands) {
            if(command.getLabel() != null && !command.getLabel().isBlank()) {
                labelsList.add(command.getLabel().trim());
            }
        }
        List<String> targetLabels = new ArrayList<>();
        for(Command command : this.commands) {
            if(command.isJumpCommand()) {
                targetLabels.add(command.getTargetLabel().trim());
            }
        }
        for(String label : targetLabels) {
            if(!labelsList.contains(label)) {
                return false;
            }
        }
        return true;
    }

    private void parseObjectToLocalVariables(SProgram program) {
        this.currentProgramName = program.getName();
        SInstructions instructions = program.getSInstructions();
        for( SInstruction instruction : instructions.getSInstruction()) {
            if(Objects.equals(instruction.getType(), "basic")){
                this.commands.add(createBaseCommandFromInstruction(instruction));
            }
            else if (Objects.equals(instruction.getType(), "synthetic")){
                this.commands.add(createSyntheticCommandFromInstruction(instruction));
            }
        }
        for(Command cmd : this.commands) {
            if(cmd instanceof SyntheticCommand) {
                ((SyntheticCommand) cmd).initializeExpandedCommands();
            }
        }
    }

    private Command createSyntheticCommandFromInstruction(SInstruction instruction) {
        return switch (instruction.getName()) {
            case "ZERO_VARIABLE" -> new ZeroVariable(instruction);
            case "GOTO_LABEL" -> new GotoLabel(instruction);
            case "ASSIGNMENT" -> new Assignment(instruction);
            case "CONSTANT_ASSIGNMENT" -> new ConstantAssignment(instruction);
            case "JUMP_ZERO" -> new JumpZero(instruction);
            case "JUMP_EQUAL_CONSTANT" -> new JumpEqualConstant(instruction);
            case "JUMP_EQUAL_VARIABLE" -> new JumpEqualVariable(instruction);
            default -> throw new IllegalArgumentException("Unknown command in file: " + instruction.getName());
        };
    }

    private Command createBaseCommandFromInstruction(SInstruction instruction) {
        return switch (instruction.getName()) {
            case "DECREASE" -> new Decrease(instruction);
            case "INCREASE" -> new Increase(instruction);
            case "NEUTRAL" -> new Neutral(instruction);
            case "JUMP_NOT_ZERO" -> new JumpNotZero(instruction);
            default -> throw new IllegalArgumentException("Unknown command in file: " + instruction.getName());
        };
    }

    public String getListOfInputParameters(int expansionLevel) {
        StringBuilder sb = new StringBuilder();
        for(Variable variable : variables) {
            if(variable instanceof InputVariable) {
                sb.append(variable.getName()).append(" ");
            }
        }

        return sb.toString().trim();
    }

    @Override
    public Set<String> getLabels(int expansionLevel) {
        Set<String> labels = new LinkedHashSet<>();
        List<Command> commandsAtLevel = getCommandsAtDesiredLevel(expansionLevel);
        for(Command command : commandsAtLevel) {
            if(command.getLabels() != null && !command.getLabels().isBlank()) {
                labels.add(command.getLabel());
            }
        }
        if(labels.contains("   ")) {
            labels.remove("   ");
        }

        List<String> sortedLabels = labels.stream().sorted().toList();
        labels = new LinkedHashSet<>(sortedLabels);

        return labels;
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public int getMaxExpansionDepth() {
        int maxDepth = 0;
        for (Command command : commands) {
            if (command.getExpansionDepth() > maxDepth) {
                maxDepth = command.getExpansionDepth();
            }
        }
        return maxDepth;
    }

    @Override
    public void SetInputVariablesValues(String[] values) {
        int index = 0;
        for( Variable variable : variables) {
            if (variable instanceof InputVariable && variable.getName().charAt(1) == (index+1)+ '0') {
                if (index < values.length) {
                    variable.setValue(Integer.parseInt(values[index]));
                    ((InputVariable) variable).setOriginalValue(Integer.parseInt(values[index]));
                }
                index++;
            }
        }
        if(index < values.length) {
            while(index < values.length) {
                InputVariable newInputVar = new InputVariable("x" + (index+1), Integer.parseInt(values[index]), false);
                newInputVar.setOriginalValue(Integer.parseInt(values[index]));
                newInputVar.setValue(Integer.parseInt(values[index]));
                //variables.add(newInputVar);
                extraInputVariables.add(newInputVar);
                index++;
            }
        }
    }

    public Set<Variable> getVariables() {
        return variables;
    }

    @Override
    public void executeProgram(int expansionLevel) {
        resetWorkAndOutputVariables();
        int index = 0;
        this.cycleSum = 0;
        List<Command> commands = getCommandsAtDesiredLevel(expansionLevel);
        Command currentCommand = commands.get(index);
        while (currentCommand != null) {
            String executionLabel = currentCommand.execute();
            this.cycleSum += currentCommand.getCycles();
            if(executionLabel != null) {
                if (executionLabel.length() == 2) {
                    executionLabel = executionLabel + " "; // Ensure label has at least 3 characters
                }
                if (executionLabel.equals("EXIT")) {
                    break; // End of program
                }
                for (Command command : commands) {
                    String currentLabel = command.getLabel();
                    if (currentLabel.equals(executionLabel)) {
                        currentCommand = command;
                        index = commands.indexOf(currentCommand);
                        break;
                    }
                }
            }
            else {
                index++;
                if (index < commands.size()) {
                    currentCommand = commands.get(index);
                } else {
                    currentCommand = null; // No more commands to execute
                }
            }
        }
        this.stats.updateStatEntry(expansionLevel, variables,extraInputVariables , cycleSum);
    }

    private void resetWorkAndOutputVariables() {
        for(Variable variable : variables) {
            if(variable instanceof WorkVariable || variable instanceof OutputVariable) {
                variable.setValue(0);
            }
        }
    }

    public void reset() {
        List<Variable> varsToRemove = new ArrayList<>();
        for(Variable variable : variables) {
            if(variable instanceof OutputVariable) {
                variable.setValue(0);
            } else if (variable instanceof InputVariable) {
                if(!((InputVariable) variable).isOriginal()) {
                    varsToRemove.add(variable);
                } else {
                    variable.setValue(0);
                }
            } else if (variable instanceof WorkVariable) {
                varsToRemove.add(variable);
            }
        }
        varsToRemove.forEach(variables::remove);
        extraInputVariables.clear();
    }

    @Override
    public void saveCurrentProgram(String filePath) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath + "\\" + this.currentProgramName))) {
            out.writeObject(this);
        } catch (Exception e) {
            throw new RuntimeException("[ERROR] Could not save the program.",e);
        }
    }

    @Override
    public int getCurrentDegree() {
        return curretDegree;
    }

    @Override
    public void increaseDegree() {
        if (this.curretDegree < getMaxExpansionDepth()) {
            this.curretDegree++;
        }
    }

    @Override
    public void decreaseDegree() {
        if (this.curretDegree > 0) {
            this.curretDegree--;
        }
    }

    @Override
    public Set<Variable> getExtraInputVariables() {
        return extraInputVariables;
    }


    @Override
    public int getCycleSum() {
        return cycleSum;
    }

    @Override
    public Stats getExecutionHistory() {
        return stats;
    }

    @Override
    public List<Command> getCommandsAtDesiredLevel(int expansionLevel) {
       List<Command> commandsAtLevel = new ArrayList<>(this.commands);
       for(int i = 0 ; i < expansionLevel ; i++) {
           for(int j = 0 ; j < commandsAtLevel.size() ; j++) {
               Command cmd = commandsAtLevel.get(j);
               if(cmd instanceof SyntheticCommand) {
                   commandsAtLevel.remove(j);
                   commandsAtLevel.addAll(j, ((SyntheticCommand) cmd).getExpandedCommands());
                     j += ((SyntheticCommand) cmd).getExpandedCommands().size() - 1;
               }
           }
       }

       return commandsAtLevel;
    }
}
