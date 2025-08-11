package engine;

import engine.arguments.Argument;
import engine.commands.Command;

import java.util.List;

public class Engine implements S_Emulator {
    private List<Command> commands;
    private String currentProgramName;
    private List<Argument> arguments;
    private List<String> labels;

    public String getCurrentProgramName() {
        return currentProgramName;
    }

    public void something() {
    }

}
