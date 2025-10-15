package engine;

import api.dto.HistoryChainDTO;
import engine.arguments.Variable;
import engine.commands.Command;

import java.util.List;
import java.util.Set;

public interface S_Emulator {

    Boolean readProgramFromXml(String xmlContent);

    Set<String> getLabels(int expansionLevel);

    List<Command> getCommands();

    int getMaxExpansionDepth();

    Set<Variable> getVariables();

    void prepareForDebugging();

    void stepOver();

    Command getCurrentDebugCommand();

    void executeProgram(int expansionLevel, boolean forHistory);

    void reset();

    int getCurrentDegree();

    void increaseDegree();

    void decreaseDegree();

    void setCurrentDegree(int degree);

    int getCycleSum();

    Stats getExecutionHistory();

    List<Command> getCommandsAtDesiredLevel(int expansionLevel);

    List<String> getParentCommandChain(int commandId, int expansionLevel);

    List<HistoryChainDTO> getParentCommandChainDTO(int commandId, int expansionLevel);

    void updateInputVariable(String name, String value);

    Program[] getSunFunctions();

    String countBasicCommands();

    String countSyntheticCommands();

    void hardReset();

    int getExecutionCount();

    double getAverageCreditCost();

    String getCurrentProgramName();
}
