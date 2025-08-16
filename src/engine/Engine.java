package engine;

import engine.arguments.Varible;
import engine.arguments.types.InputVarible;
import engine.commands.Command;
import engine.commands.base.types.*;
import engine.commands.synthetic.types.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import schema.*;

import java.io.File;
import java.util.*;

public class Engine implements S_Emulator {
    private List<Command> commands;
    private String currentProgramName;
    public static Set<Varible> varibles;

    public Engine() {
        this.commands = new ArrayList<>();
        varibles = new LinkedHashSet<>();
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
            }
        } catch (JAXBException ignored) {}
        return found;
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
            default -> null;
        };
    }

    private Command createBaseCommandFromInstruction(SInstruction instruction) {
        return switch (instruction.getName()) {
            case "DECREASE" -> new Decrease(instruction);
            case "INCREASE" -> new Increase(instruction);
            case "NEUTRAL" -> new Neutral(instruction);
            case "JUMP_NOT_ZERO" -> new JumpNotZero(instruction);
            default -> null;
        };
    }

    public String getListOfInputParameters() {
        StringBuilder sb = new StringBuilder();
        for (Varible varible : varibles) {
            if (varible instanceof InputVarible) {
                sb.append(varible.getName()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public String getLabels() {
        StringBuilder sb = new StringBuilder();
        for (Command command : commands) {
            if(command == null) {
                continue; // Skip null commands
            }
            if (!Objects.equals(command.getLabel(), "  ")) {
                sb.append(command.getLabel()).append(" ");
            }
        }
        return sb.toString().trim();
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
        for( Varible varible : varibles) {
            if (varible instanceof InputVarible && varible.getName().charAt(1) == (index+1)+ '0') {
                if (index < values.length) {
                    varible.setValue(Integer.parseInt(values[index]));
                }
                index++;
            }
        }
        if(index < values.length) {
            while(index < values.length) {
                varibles.add(new InputVarible("x" + (index+1), Integer.parseInt(values[index])));
                index++;
            }
        }
    }

    public Set<Varible> getVariables() {
        return varibles;
    }

    @Override
    public void executeProgram(int expansionLevel) {
        int index = 0;
        Command currentCommand = this.commands.get(index);
        while (currentCommand != null) {
            String executionLabel = currentCommand.execute(expansionLevel);
            if(executionLabel != null) {
                if (executionLabel.length() == 2) {
                    executionLabel = executionLabel + " "; // Ensure label has at least 3 characters
                }
                if (executionLabel.equals("END")) {
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
    }
}
