package engine;

import engine.commands.Command;
import java.util.List;

public interface S_Emulator {
    String getCurrentProgramName();

    void readProgramFromXml();

    String getListOfInputParameters();

    String getLabels();

    List<Command> getCommands();

    int getMaxExpansionDepth();
}
