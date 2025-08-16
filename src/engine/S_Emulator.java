package engine;

import com.sun.jdi.connect.Connector;
import engine.arguments.Varible;
import engine.commands.Command;
import java.util.List;
import java.util.Set;

public interface S_Emulator {
    String getCurrentProgramName();

    Boolean readProgramFromXml(String filePath);

    String getListOfInputParameters();

    String getLabels();

    List<Command> getCommands();

    int getMaxExpansionDepth();

    void SetInputVariablesValues(String[] values);

    Set<Varible> getVariables();

    void executeProgram(int expansionLevel);
}
