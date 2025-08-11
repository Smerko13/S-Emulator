package engine;

import engine.arguments.Argument;
import engine.commands.Command;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import schema.*;

import java.io.File;
import java.util.List;

public class Engine implements S_Emulator {
    private List<Command> commands;
    private String currentProgramName;

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
            System.out.print("\n");
            SInstructionArguments args = instruction.getSInstructionArguments();
            if( args != null ) {
                for (SInstructionArgument arg : args.getSInstructionArgument()) {
                    System.out.println(arg.getName());
                    System.out.println(arg.getValue());
                }
            }
            else
            {
                System.out.println("No arguments for instruction: " + instruction.getName());
            }
            if(instruction.getSVariable() != null) {
                System.out.println(instruction.getSVariable());
            }
            else {
                System.out.println("No variable for instruction: " + instruction.getName());
            }

            if( instruction.getSLabel() != null ) {
                System.out.println(instruction.getSLabel());
            }
            else {
                System.out.println("No label for instruction: " + instruction.getName());
            }
            if(instruction.getName()!= null)
                System.out.println("Instruction name: " + instruction.getName());
            else
                System.out.println("No name for instruction");

            if(instruction.getType() != null) {
                System.out.println(instruction.getType());
            }
            else {
                System.out.println("No type for instruction: " + instruction.getName());
            }
            System.out.print("\n");
        }
    }
}
