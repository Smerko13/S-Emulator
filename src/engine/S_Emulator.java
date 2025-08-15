package engine;

import engine.commands.Command;
import java.util.List;

public interface S_Emulator {
    String getCurrentProgramName();

    Boolean readProgramFromXml(String filePath);

    String getListOfInputParameters();

    String getLabels();

    List<Command> getCommands();

    int getMaxExpansionDepth();
}
