package engine.commands;

public abstract class Command {
    private static int commandIdTracker = 1;
    private int commandId = 0;
    protected String label = "  "; // Default label
    protected int cycles;
    protected char commandType;
    protected Boolean isExpandable;
    protected String commandName;

    public Command() {
        this.commandId = commandIdTracker;
        commandIdTracker++;
    }
}
