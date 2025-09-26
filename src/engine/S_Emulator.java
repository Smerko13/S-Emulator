package engine;

import engine.arguments.Variable;
import engine.commands.Command;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Set;

public interface S_Emulator {
    String getCurrentProgramName();

    Boolean readProgramFromXml(String filePath);

    Set<String> getLabels(int expansionLevel);

    List<Command> getCommands();

    int getMaxExpansionDepth();

    Set<Variable> getVariables();

    void executeProgram(int expansionLevel);

    int getCycleSum();

    Stats getExecutionHistory();

    List<Command> getCommandsAtDesiredLevel(int expansionLevel);

    void reset();

    static Engine loadSavedProgram(String filePath) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Engine) in.readObject();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    int getCurrentDegree();

    void increaseDegree();

    void decreaseDegree();

    void prepareForDebugging();

    void stepOver();

    Command getCurrentDebugCommand();

    Engine[] getSunFunctions();

    String countBasicCommands();

    String countSyntheticCommands();
}
