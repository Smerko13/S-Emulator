package components.mainDashboard.users;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ExecutionHistoryRow {
    private final SimpleIntegerProperty runId;
    private final SimpleStringProperty executionType;
    private final SimpleStringProperty programFunctionName;
    private final SimpleStringProperty architectureType;
    private final SimpleIntegerProperty expansionDegree;  // Changed from String to Integer
    private final SimpleIntegerProperty cpuCyclesUsed;
    private final SimpleIntegerProperty finalYValue;

    public ExecutionHistoryRow(int runId, String executionType, String programFunctionName,
                               String architectureType, int expansionDegree,
                               int cpuCyclesUsed, int finalYValue) {
        this.runId = new SimpleIntegerProperty(runId);
        this.executionType = new SimpleStringProperty(executionType);
        this.programFunctionName = new SimpleStringProperty(programFunctionName);
        this.architectureType = new SimpleStringProperty(architectureType);
        this.expansionDegree = new SimpleIntegerProperty(expansionDegree);  // Now stores the degree number
        this.cpuCyclesUsed = new SimpleIntegerProperty(cpuCyclesUsed);
        this.finalYValue = new SimpleIntegerProperty(finalYValue);
    }

    public SimpleIntegerProperty runIdProperty() {
        return runId;
    }

    public SimpleStringProperty executionTypeProperty() {
        return executionType;
    }

    public SimpleStringProperty programFunctionNameProperty() {
        return programFunctionName;
    }

    public SimpleStringProperty architectureTypeProperty() {
        return architectureType;
    }

    public SimpleIntegerProperty expansionDegreeProperty() {  // Changed return type
        return expansionDegree;
    }

    public SimpleIntegerProperty cpuCyclesUsedProperty() {
        return cpuCyclesUsed;
    }

    public SimpleIntegerProperty finalYValueProperty() {
        return finalYValue;
    }

    public int getRunId() {
        return runId.get();
    }
}
