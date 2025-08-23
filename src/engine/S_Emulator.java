package engine;

import engine.arguments.Variable;
import engine.commands.Command;
import java.util.List;
import java.util.Set;

public interface S_Emulator {
    String getCurrentProgramName();

    Boolean readProgramFromXml(String filePath);

    String getListOfInputParameters();

    Set<String> getLabels();

    List<Command> getCommands();

    int getMaxExpansionDepth();

    void SetInputVariablesValues(String[] values);

    Set<Variable> getVariables();

    void executeProgram(int expansionLevel);

    int getCycleSum();

    Stats getExecutionHistory();

    List<Command> getCommandsAtDesiredLevel(int expansionLevel);

    void arrangeIDs(int expansionLevel);
}
