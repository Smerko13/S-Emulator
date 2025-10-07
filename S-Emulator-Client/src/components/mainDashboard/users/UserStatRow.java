package components.mainDashboard.users;

import javafx.beans.property.*;

public class UserStatRow {
    private final IntegerProperty executionNumber = new SimpleIntegerProperty();
    private final StringProperty runType = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty architecture = new SimpleStringProperty();
    private final IntegerProperty expansionLevel = new SimpleIntegerProperty();
    private final IntegerProperty cycles = new SimpleIntegerProperty();
    private final StringProperty output = new SimpleStringProperty();

    public UserStatRow(int exec, String runType, String name, String arch, int exp, int cycles, String out) {
        setExecutionNumber(exec); setRunType(runType); setName(name);
        setArchitecture(arch); setExpansionLevel(exp); setCycles(cycles); setOutput(out);
    }

    public IntegerProperty executionNumberProperty() { return executionNumber; }
    public StringProperty runTypeProperty() { return runType; }
    public StringProperty nameProperty() { return name; }
    public StringProperty architectureProperty() { return architecture; }
    public IntegerProperty expansionLevelProperty() { return expansionLevel; }
    public IntegerProperty cyclesProperty() { return cycles; }
    public StringProperty outputProperty() { return output; }

    public void setExecutionNumber(int v) { executionNumber.set(v); }
    public void setRunType(String v) { runType.set(v); }
    public void setName(String v) { name.set(v); }
    public void setArchitecture(String v) { architecture.set(v); }
    public void setExpansionLevel(int v) { expansionLevel.set(v); }
    public void setCycles(int v) { cycles.set(v); }
    public void setOutput(String v) { output.set(v); }
}
