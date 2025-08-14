package engine;

import engine.arguments.Argument;
import engine.commands.Command;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.Increase;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import schema.*;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Engine implements S_Emulator {
    private List<Command> commands;
    private String currentProgramName;
    public static Set<Argument> arguments;

    public Engine() {
        this.commands = new java.util.ArrayList<>();
        arguments = new java.util.HashSet<>();
    }

    public String getCurrentProgramName() {
        return currentProgramName;
    }

    public void readProgramFromXml() {
        try {
            File xmlFile = new File("src/programs/basic.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(SProgram.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SProgram program = (SProgram) jaxbUnmarshaller.unmarshal(xmlFile);
            parseObjectToLocalVariables(program);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void parseObjectToLocalVariables(SProgram program) {
        this.currentProgramName = program.getName();
        SInstructions instructions = program.getSInstructions();
        for( SInstruction instruction : instructions.getSInstruction()) {
            if(Objects.equals(instruction.getType(), "basic")){
                this.commands.add(createBaseCommandFromInstruction(instruction));
            }
            else if (Objects.equals(instruction.getType(), "synthetic")){
                System.out.println("Got a synthetic instruction: " + instruction.getName());
            }
            else {
                System.out.println("Unknown instruction type: " + instruction.getType());
            }
        }
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
        for (Argument argument : arguments) {
            if (argument instanceof engine.arguments.types.InputArgument) {
                sb.append(argument.getName()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public String getLabels() {
        StringBuilder sb = new StringBuilder();
        for (Command command : commands) {
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


}
