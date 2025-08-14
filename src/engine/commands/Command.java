package engine.commands;

import engine.Engine;
import engine.arguments.Argument;
import engine.arguments.types.OutputArgument;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {
    private static int commandIdTracker = 1;
    private int commandId = 0;
    protected String label = "  "; // Default label
    protected int cycles;
    protected char commandType;
    protected Boolean isExpandable;
    protected String commandName;
    protected List<Argument> arguments;
    protected int levelOfExpansion;

    public Command() {
        this.commandId = commandIdTracker;
        commandIdTracker++;
        arguments = new ArrayList<>();
    }

    public String getLabel() {
        return label;
    }

    public String getCommandRepresentation() {
        return String.format("#%d (%c) [ %s ] %s (%d)",
                             commandId,
                             commandType,
                             label,
                             this.toString(),
                             cycles);
    }


    public int getExpansionDepth() {
        return levelOfExpansion;
    }
}
