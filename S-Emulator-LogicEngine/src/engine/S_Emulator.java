package engine;

import engine.arguments.Variable;
import engine.commands.Command;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Set;

public interface S_Emulator {

    String getCurrentProgramName();

    //public Boolean readProgramFromXml(File xmlFile);

    public Boolean readProgramFromXml(String xmlContent);

    //Boolean readProgramFromXml(String filePath);

    Set<String> getLabels(int expansionLevel);

    List<Command> getCommands();

    int getMaxExpansionDepth();

    Set<Variable> getVariables();

    void executeProgram(int expansionLevel, boolean forHistory);

    int getCycleSum();

    Stats getExecutionHistory();

    List<Command> getCommandsAtDesiredLevel(int expansionLevel);

    void reset();

    static Program loadSavedProgram(String filePath) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Program) in.readObject();
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

    Program[] getSunFunctions();

    String countBasicCommands();

    String countSyntheticCommands();

    void hardReset();

    int getExecutionCount();

    double getAverageCreditCost();
}
