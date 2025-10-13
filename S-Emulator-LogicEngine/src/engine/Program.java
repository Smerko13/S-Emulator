package engine;

import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.BaseCommand;
import engine.commands.base.types.*;
import engine.commands.synthetic.SyntheticCommand;
import engine.commands.synthetic.types.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import schema.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Program implements S_Emulator , Serializable, Cloneable {
    private List<Command> commands;
    private String currentProgramName;
    public Set<Variable> variables;
    public Set<Variable> extraInputVariables;
    private int cycleSum;
    private Stats stats;
    public  Set<String> labels;
    private int currentDegree = 0;
    private Command currentCommand;
    public  List<Program> subFunctions;
    private String userString = null;
    boolean isOriginal = false;
    public Program assosciatedProgram = null;


    public Program(boolean isOriginal) {
        this.isOriginal = isOriginal;
        this.commands = new ArrayList<>();
        variables = new LinkedHashSet<>();
        extraInputVariables = new LinkedHashSet<>();
        this.stats = new Stats();
        labels = new LinkedHashSet<>();
        subFunctions = new ArrayList<>();
        OutputVariable outputVar = new OutputVariable(true);
    }

    public String getCurrentProgramName() {
        return currentProgramName;
    }

//    public Boolean readProgramFromXml(File xmlFile) {
//        boolean found = false;
//        try {
//                found = true;
//                JAXBContext jaxbContext = JAXBContext.newInstance(SProgram.class);
//                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//                SProgram program = (SProgram) jaxbUnmarshaller.unmarshal(xmlFile);
//                parseObjectToLocalVariables(program);
//                if(!checkIfProgramIsValid()) {
//                    return false;
//                }
//                this.stats.reset();
//        } catch (JAXBException e) {
//            return false;
//        }
//        return found;
//    }
//
//    public Boolean readProgramFromXml(String filePath) {
//        boolean found = false;
//        try {
//            File xmlFile = new File(filePath);
//            if (xmlFile.exists()) {
//                found = true;
//                JAXBContext jaxbContext = JAXBContext.newInstance(SProgram.class);
//                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//                SProgram program = (SProgram) jaxbUnmarshaller.unmarshal(xmlFile);
//                parseObjectToLocalVariables(program);
//                if(!checkIfProgramIsValid()) {
//                    return false;
//                }
//                this.stats.reset();
//            }
//        } catch (JAXBException e) {
//            return false;
//        }
//        return found;
//    }

    public Boolean readProgramFromXml(String xmlContent) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SProgram.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // Create StringReader from XML content
            StringReader reader = new StringReader(xmlContent);
            SProgram program = (SProgram) jaxbUnmarshaller.unmarshal(reader);

            parseObjectToLocalVariables(program);
            if(!checkIfProgramIsValid()) {
                return false;
            }
            this.stats.reset();
            return true;
        } catch (JAXBException e) {
            System.out.println("JAXB Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("General Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
        if(program.getSFunctions() != null) {
            for (SFunction function : program.getSFunctions().getSFunction()) {
                Program subProgram = new Program(false);
                subProgram.assosciatedProgram = this;
                subProgram.currentProgramName = function.getName();
                subProgram.userString = function.getUserString();
                SInstructions funcInstructions = function.getSInstructions();
                for (SInstruction instruction : funcInstructions.getSInstruction()) {
                    if (Objects.equals(instruction.getType(), "basic")) {
                        subProgram.commands.add(createBaseCommandFromInstruction(instruction));
                    } else if (Objects.equals(instruction.getType(), "synthetic")) {
                        subProgram.commands.add(createSyntheticCommandFromInstruction(instruction));
                    }
                }
                for (Command cmd : subProgram.commands) {
                    subProgram.variables.addAll(List.of(cmd.getAssociatedVariables()));
                }
                // Initialize expanded commands for synthetic commands in sub-functions
                for(Command cmd : subProgram.commands) {
                    if (cmd instanceof SyntheticCommand) {
                        ((SyntheticCommand) cmd).initializeExpandedCommands();
                    }
                }
                this.subFunctions.add(subProgram);
            }
        }
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
            if (cmd instanceof SyntheticCommand) {
                ((SyntheticCommand) cmd).initializeExpandedCommands();
            }
        }
    }

    private Command createSyntheticCommandFromInstruction(SInstruction instruction) {
        return switch (instruction.getName()) {
            case "ZERO_VARIABLE" -> new ZeroVariable(instruction, this);
            case "GOTO_LABEL" -> new GotoLabel(instruction, this);
            case "ASSIGNMENT" -> new Assignment(instruction, this);
            case "CONSTANT_ASSIGNMENT" -> new ConstantAssignment(instruction, this);
            case "JUMP_ZERO" -> new JumpZero(instruction,this);
            case "JUMP_EQUAL_CONSTANT" -> new JumpEqualConstant(instruction, this);
            case "JUMP_EQUAL_VARIABLE" -> new JumpEqualVariable(instruction,this);
            case "QUOTE" -> new Quote(instruction,this);
            case "JUMP_EQUAL_FUNCTION" -> new JumpEqualFunction(instruction,this);
            default -> throw new IllegalArgumentException("Unknown command in file: " + instruction.getName());
        };
    }

    private Command createBaseCommandFromInstruction(SInstruction instruction) {
        return switch (instruction.getName()) {
            case "DECREASE" -> new Decrease(instruction, this);
            case "INCREASE" -> new Increase(instruction, this);
            case "NEUTRAL" -> new Neutral(instruction, this);
            case "JUMP_NOT_ZERO" -> new JumpNotZero(instruction, this);
            default -> throw new IllegalArgumentException("Unknown command in file: " + instruction.getName());
        };
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
        for (Command command : this.commands) {
            if (command.getExpansionDepth() > maxDepth) {
                maxDepth = command.getExpansionDepth();
            }
        }
        return maxDepth;
    }

    public Set<Variable> getVariables() {
        return variables;
    }

    @Override
    public void prepareForDebugging() {
        resetWorkAndOutputVariables();
        this.currentCommand = getCommandsAtDesiredLevel(currentDegree).getFirst();
    }

    @Override
    public void stepOver() {
        if (this.currentCommand != null) {
            String executionLabel = this.currentCommand.execute();
            this.cycleSum += this.currentCommand.getCycles();
            if(executionLabel != null) {
                if (executionLabel.length() == 2) {
                    executionLabel = executionLabel + " "; // Ensure label has at least 3 characters
                }
                if (executionLabel.equals("EXIT")) {
                    this.currentCommand = null; // End of program
                    return;
                }
                for (Command command : getCommandsAtDesiredLevel(currentDegree)) {
                    String currentLabel = command.getLabel();
                    if (currentLabel.equals(executionLabel)) {
                        this.currentCommand = command;
                        break;
                    }
                }
            }
            else {
                int currentIndex = getCommandsAtDesiredLevel(currentDegree).indexOf(this.currentCommand);
                currentIndex++;
                if (currentIndex < getCommandsAtDesiredLevel(currentDegree).size()) {
                    this.currentCommand = getCommandsAtDesiredLevel(currentDegree).get(currentIndex);
                } else {
                    this.currentCommand = null; // No more commands to execute
                }
            }
        }
    }

    @Override
    public Command getCurrentDebugCommand() {
        return this.currentCommand;
    }

    @Override
    public Program[] getSunFunctions() {
        List<Program> funcs = new ArrayList<>(this.subFunctions);
        for(Program subFunction : subFunctions) {
            funcs.addAll(Arrays.asList(subFunction.getSunFunctions()));
        }
        return funcs.toArray(new Program[0]);
    }

    @Override
    public String countBasicCommands() {
        int i = 0;
        for(Command cmd : this.getCommandsAtDesiredLevel(currentDegree)) {
            if(cmd instanceof BaseCommand) {
                i++;
            }
        }
        return String.valueOf(i);
    }

    @Override
    public String countSyntheticCommands() {
        int i = 0;
        for(Command cmd : this.getCommandsAtDesiredLevel(currentDegree)) {
            if(cmd instanceof SyntheticCommand) {
                i++;
            }
        }
        return String.valueOf(i);
    }

    @Override
    public void hardReset() {
        for(Variable variable : variables) {
            variable.setValue(0);
        }
        this.cycleSum = 0;
        this.currentDegree = 0;
        this.currentCommand = null;
    }

    @Override
    public int getExecutionCount() {
        return 0;
    }

    @Override
    public double getAverageCreditCost() {
        return 0;
    }


    @Override
    public void executeProgram(int expansionLevel,boolean forHistory) {
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

        if(forHistory) {
            this.stats.updateStatEntry(expansionLevel, variables, extraInputVariables, cycleSum);
        }
    }

    public void resetWorkAndOutputVariables() {
        if(this.assosciatedProgram != null) {
            for (Variable variable : this.assosciatedProgram.variables) {
                if (variable instanceof WorkVariable || variable instanceof OutputVariable) {
                    variable.setValue(0);
                }
            }
        } else {
            for (Variable variable : variables) {
                if (variable instanceof WorkVariable || variable instanceof OutputVariable) {
                    variable.setValue(0);
                }
            }
        }
    }

    public void reset() {
        for(Variable variable : variables) {
            if(variable instanceof OutputVariable) {
                variable.setValue(0);
            } else if (variable instanceof InputVariable) {
                if(!((InputVariable) variable).isOriginal()) {
                    variable.setValue(0);
                } else {
                    variable.setValue(((InputVariable) variable).getOriginalValue());
                }
            } else if (variable instanceof WorkVariable) {
                variable.setValue(0);
            }
        }
        this.cycleSum = 0;
        this.currentCommand = null;
    }

    @Override
    public int getCurrentDegree() {
        return currentDegree;
    }

    @Override
    public void increaseDegree() {
        if (this.currentDegree < getMaxExpansionDepth()) {
            this.currentDegree++;
        }
    }

    @Override
    public void decreaseDegree() {
        if (this.currentDegree > 0) {
            this.currentDegree--;
        }
    }

    @Override
    public void setCurrentDegree(int degree) {
        if (degree >= 0 && degree <= getMaxExpansionDepth()) {
            this.currentDegree = degree;
        }
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

       int id = 1;
       for(Command cmd : commandsAtLevel) {
           cmd.setID(id++);
       }

       return commandsAtLevel;
    }

    public String getUserString() {
        return userString;
    }

    public void setUserString(String userString) {
        this.userString = userString;
    }

    public int executeFunction(List<Variable> variables,String functionName, Program associatedProgram) {
        LinkedList<Variable> varsCopy = new LinkedList<>();
        for(Variable v : variables) {
            Variable copy = new WorkVariable(v.getName());
            copy.setValue(v.getValue());
            varsCopy.add(copy);
        }

         for(Program e : associatedProgram.subFunctions) {
            if(e.getCurrentProgramName().equals(functionName)  || e.getUserString().equals(functionName)) {
                Set<Variable> snapshot = associatedProgram.getVariables().stream()
                        .map(v -> v.clone())
                        .collect(Collectors.toSet());
                e.assignVarsToCommands(varsCopy);
                e.executeProgram(0,false);
                int returnValue = e.getReturnValue();
                for(Variable var : snapshot) {
                    for(Variable originalVar : associatedProgram.getVariables()) {
                        if(var.getName().equals(originalVar.getName())) {
                            originalVar.setValue(var.getValue());
                        }
                    }
                }
                return returnValue;
            }
        }

         int returnValue = -1;
         return -1;
    }

    private void assignVarsToCommands(List<Variable> variables) {
        if(variables.isEmpty()) {return;}
        Variable currentVar = variables.getFirst();
        for(Variable v : this.variables) {
            if(v instanceof InputVariable) {
                v.setValue(currentVar.getValue());
                if(variables.indexOf(currentVar) + 1 < variables.size()) {
                    currentVar = variables.get(variables.indexOf(currentVar) + 1);
                }
            }
        }
    }

    public int getTotalCycles() {
        int totalCycles = 0;
        for(Command cmd : this.commands) {
            totalCycles += cmd.getCycles();
        }
        return totalCycles;
    }

    @Override
    public Program clone() {
        try {
            Program cloned = (Program) super.clone();
            // Deep copy commands
            cloned.commands = new ArrayList<>();
            for (Command cmd : this.commands) {
                cloned.commands.add(cmd.clone());
            }
            // Deep copy variables
            cloned.variables = new LinkedHashSet<>();
            for (Variable var : this.variables) {
                cloned.variables.add(var.clone());
            }
            // Deep copy extraInputVariables
            cloned.extraInputVariables = new LinkedHashSet<>();
            for (Variable var : this.extraInputVariables) {
                cloned.extraInputVariables.add(var.clone());
            }
            // Deep copy labels
            cloned.labels = new LinkedHashSet<>(this.labels);
            // Deep copy stats
            cloned.stats = this.stats != null ? this.stats.clone() : null;
            // Deep copy subFunctions
            cloned.subFunctions = new ArrayList<>();
            for (Program sub : this.subFunctions) {
                cloned.subFunctions.add(sub.clone());
            }
            // Strings and primitives are immutable, so no need to clone
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public Variable getOutputVar() {
        return this.variables.stream().filter(v -> v instanceof OutputVariable).findFirst().orElse(null);
    }

    public int getReturnValue() {
        for(Variable var : this.variables) {
            if(var instanceof OutputVariable) {
                return var.getValue();
            }
        }
        return 0;
    }

    public void setVariables(ArrayList<Variable> variables) {
        this.variables.addAll(variables);
    }

    public void expandCommands() {
        for(Command cmd : this.commands) {
            if(cmd instanceof SyntheticCommand) {
                ((SyntheticCommand) cmd).initializeExpandedCommands();
            }
        }
    }

    @Override
    public List<String> getParentCommandChain(int commandId, int expansionLevel) {
        List<String> parentChain = new ArrayList<>();

        if (expansionLevel == 0) {
            parentChain.add("Root level - no parent commands");
            return parentChain;
        }

        // Build the command chain by reconstructing the expansion process
        // We'll trace back through each expansion level to find the parent commands

        // Start with the target command at the current level
        List<Command> currentLevelCommands = getCommandsAtDesiredLevel(expansionLevel);
        Command targetCommand = null;
        for (Command cmd : currentLevelCommands) {
            if (cmd.getId() == commandId) {
                targetCommand = cmd;
                break;
            }
        }

        if (targetCommand == null) {
            parentChain.add("Command not found at expansion level " + expansionLevel);
            return parentChain;
        }

        // Now trace back through the expansion levels
        Command currentTraceCommand = targetCommand;

        for (int level = expansionLevel - 1; level >= 0; level--) {
            if (currentTraceCommand == null) break;

            // Find the parent command that this command expanded from
            Command parentCommand = findParentAtLevel(currentTraceCommand, level);

            if (parentCommand != null) {
                // Get the commands at this level to find the correct ID
                List<Command> levelCommands = getCommandsAtDesiredLevel(level);
                int parentId = -1;
                for (int i = 0; i < levelCommands.size(); i++) {
                    Command cmd = levelCommands.get(i);
                    if (isSameCommand(cmd, parentCommand)) {
                        parentId = i + 1; // IDs start from 1
                        break;
                    }
                }

                String commandInfo = String.format("Level %d: [ID: %d] %s",
                    level, parentId, parentCommand.toString());
                parentChain.add(0, commandInfo);

                currentTraceCommand = parentCommand;
            } else {
                // No parent found at this level - might be a direct command
                break;
            }
        }

        if (parentChain.isEmpty()) {
            parentChain.add("No parent command chain found");
        }

        return parentChain;
    }

    private Command findParentAtLevel(Command targetCommand, int level) {
        // If the target command has a parent reference, use it
        Command directParent = targetCommand.getParentCommand();
        if (directParent != null) {
            return directParent;
        }

        // If no direct parent, we need to find which command at the previous level
        // would have expanded to include this command
        List<Command> levelCommands = getCommandsAtDesiredLevel(level);

        for (Command cmd : levelCommands) {
            if (cmd instanceof SyntheticCommand) {
                SyntheticCommand synCmd = (SyntheticCommand) cmd;
                List<Command> expandedCommands = synCmd.getExpandedCommands();

                // Check if any of the expanded commands match our target
                for (Command expanded : expandedCommands) {
                    if (isSameCommand(expanded, targetCommand)) {
                        return cmd;
                    }
                }
            }
        }

        return null;
    }

    private boolean isSameCommand(Command cmd1, Command cmd2) {
        if (cmd1 == cmd2) return true;
        if (cmd1 == null || cmd2 == null) return false;

        // Compare by class, variable, and label
        return cmd1.getClass().equals(cmd2.getClass()) &&
               Objects.equals(getVariableName(cmd1), getVariableName(cmd2)) &&
               Objects.equals(cmd1.getLabel(), cmd2.getLabel());
    }

    private String getVariableName(Command cmd) {
        Variable var = cmd.getVariable();
        return var != null ? var.getName() : null;
    }

    @Override
    public void updateInputVariable(String name, String value) {
        System.out.println("Program.updateInputVariable called with name: '" + name + "', value: '" + value + "'");

        try {
            int intValue = Integer.parseInt(value);

            // First, try to find and update the variable in the main variables set
            boolean variableFound = false;
            for (Variable variable : variables) {
                if (variable instanceof InputVariable && variable.getName().equals(name)) {
                    variable.setValue(intValue);
                    variableFound = true;
                    System.out.println("Updated input variable " + name + " to " + intValue + " in main variables set");
                    break;
                }
            }

            // Also check extraInputVariables set
            for (Variable variable : extraInputVariables) {
                if (variable instanceof InputVariable && variable.getName().equals(name)) {
                    variable.setValue(intValue);
                    variableFound = true;
                    System.out.println("Updated input variable " + name + " to " + intValue + " in extra input variables set");
                    break;
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid value format for input variable " + name + ": " + value);
            throw new IllegalArgumentException("Invalid value format: " + value);
        }
    }
}
