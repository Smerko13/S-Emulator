package components.mainDashboard.ProgramsAndFunctions;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ProgramRow {
    private final SimpleStringProperty programName;
    private final SimpleStringProperty uploaderName;
    private final SimpleIntegerProperty instructionCount;
    private final SimpleIntegerProperty maxLevel;
    private final SimpleIntegerProperty executionsCount;
    private final SimpleDoubleProperty avgCreditCost;

    public ProgramRow(String programName, String uploaderName, int instructionCount,
                     int maxLevel, int executionsCount, double avgCreditCost) {
        this.programName = new SimpleStringProperty(programName);
        this.uploaderName = new SimpleStringProperty(uploaderName);
        this.instructionCount = new SimpleIntegerProperty(instructionCount);
        this.maxLevel = new SimpleIntegerProperty(maxLevel);
        this.executionsCount = new SimpleIntegerProperty(executionsCount);
        this.avgCreditCost = new SimpleDoubleProperty(avgCreditCost);
    }

    public SimpleStringProperty programNameProperty() {
        return programName;
    }

    public SimpleStringProperty uploaderNameProperty() {
        return uploaderName;
    }

    public SimpleIntegerProperty instructionCountProperty() {
        return instructionCount;
    }

    public SimpleIntegerProperty maxLevelProperty() {
        return maxLevel;
    }

    public SimpleIntegerProperty executionsCountProperty() {
        return executionsCount;
    }

    public SimpleDoubleProperty avgCreditCostProperty() {
        return avgCreditCost;
    }
}

