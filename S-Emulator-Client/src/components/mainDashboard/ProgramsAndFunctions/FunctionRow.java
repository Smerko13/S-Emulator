package components.mainDashboard.ProgramsAndFunctions;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class FunctionRow {
    private final SimpleStringProperty functionName;
    private final SimpleStringProperty parentProgram;
    private final SimpleStringProperty uploaderName;
    private final SimpleIntegerProperty instructionCount;
    private final SimpleIntegerProperty maxLevel;

    public FunctionRow(String functionName, String parentProgram, String uploaderName,
                      int instructionCount, int maxLevel) {
        this.functionName = new SimpleStringProperty(functionName);
        this.parentProgram = new SimpleStringProperty(parentProgram);
        this.uploaderName = new SimpleStringProperty(uploaderName);
        this.instructionCount = new SimpleIntegerProperty(instructionCount);
        this.maxLevel = new SimpleIntegerProperty(maxLevel);
    }

    public SimpleStringProperty functionNameProperty() {
        return functionName;
    }

    public SimpleStringProperty parentProgramProperty() {
        return parentProgram;
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
}

