package components.mainDashboard.users;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class ExecutionHistoryRow {
    private final IntegerProperty runId = new SimpleIntegerProperty();
    private final StringProperty executionType = new SimpleStringProperty();
    private final StringProperty programFunctionName = new SimpleStringProperty();
    private final StringProperty architectureType = new SimpleStringProperty();
    private final StringProperty executionLevel = new SimpleStringProperty();
    private final IntegerProperty cpuCyclesUsed = new SimpleIntegerProperty();
    private final IntegerProperty finalYValue = new SimpleIntegerProperty();

    public ExecutionHistoryRow() {}

    public ExecutionHistoryRow(int runId, String executionType, String programFunctionName,
                              String architectureType, String executionLevel,
                              int cpuCyclesUsed, int finalYValue) {
        this.runId.set(runId);
        this.executionType.set(executionType);
        this.programFunctionName.set(programFunctionName);
        this.architectureType.set(architectureType);
        this.executionLevel.set(executionLevel);
        this.cpuCyclesUsed.set(cpuCyclesUsed);
        this.finalYValue.set(finalYValue);
    }

    // Property getters
    public IntegerProperty runIdProperty() { return runId; }
    public StringProperty executionTypeProperty() { return executionType; }
    public StringProperty programFunctionNameProperty() { return programFunctionName; }
    public StringProperty architectureTypeProperty() { return architectureType; }
    public StringProperty executionLevelProperty() { return executionLevel; }
    public IntegerProperty cpuCyclesUsedProperty() { return cpuCyclesUsed; }
    public IntegerProperty finalYValueProperty() { return finalYValue; }

    // Value getters
    public int getRunId() { return runId.get(); }
    public String getExecutionType() { return executionType.get(); }
    public String getProgramFunctionName() { return programFunctionName.get(); }
    public String getArchitectureType() { return architectureType.get(); }
    public String getExecutionLevel() { return executionLevel.get(); }
    public int getCpuCyclesUsed() { return cpuCyclesUsed.get(); }
    public int getFinalYValue() { return finalYValue.get(); }

    // Value setters
    public void setRunId(int runId) { this.runId.set(runId); }
    public void setExecutionType(String executionType) { this.executionType.set(executionType); }
    public void setProgramFunctionName(String programFunctionName) { this.programFunctionName.set(programFunctionName); }
    public void setArchitectureType(String architectureType) { this.architectureType.set(architectureType); }
    public void setExecutionLevel(String executionLevel) { this.executionLevel.set(executionLevel); }
    public void setCpuCyclesUsed(int cpuCyclesUsed) { this.cpuCyclesUsed.set(cpuCyclesUsed); }
    public void setFinalYValue(int finalYValue) { this.finalYValue.set(finalYValue); }
}
