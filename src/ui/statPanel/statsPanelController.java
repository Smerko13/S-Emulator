package ui.statPanel;

import engine.Stats;
import engine.arguments.types.OutputVariable;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.base.BaseController;

public class statsPanelController {
    @FXML private TableView<ExecutionRecord> statsTable;
    @FXML private TableColumn<ExecutionRecord, Number> executionNumberColumn;
    @FXML private TableColumn<ExecutionRecord, Number> expansionLevelColumn;
    @FXML private TableColumn<ExecutionRecord, Number> cyclesColumn;
    @FXML private TableColumn<ExecutionRecord, String> outputColumn;
    @FXML private Button showStatusButton;
    @FXML private Button reRunButton;
    private BaseController mainController;

    public void setMainController(BaseController baseController) {
        this.mainController = baseController;
    }

    @FXML
    private void initialize() {
        executionNumberColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getExecutionNumber()));
        expansionLevelColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getExpansionLevel()));
        cyclesColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getCycles()));
        outputColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getOutput()));
    }

    public void showStatusButtonPressed(ActionEvent actionEvent) {
    }

    public void reRunButtonPressed(ActionEvent actionEvent) {
    }

    public void updateStats(Stats executionHistory) {
        ObservableList<ExecutionRecord> records = FXCollections.observableArrayList();
        for (Stats.Execution record : executionHistory.getExecutionHistory()) {
            int execNum = getExecutionNumber(record);
            int expansion = getExpansionLevel(record);
            int cycles = getCycleCount(record);
            String output = getOutputValue(record);
            records.add(new ExecutionRecord(execNum, expansion, cycles, output));
        }
        statsTable.setItems(records);
    }

    private int getExecutionNumber(Stats.Execution record) {
        return record.getExecutionNumber();
    }

    private int getExpansionLevel(Stats.Execution record) {
        try {
            var field = record.getClass().getDeclaredField("expansionLevel");
            field.setAccessible(true);
            return field.getInt(record);
        } catch (Exception e) {
            return 0;
        }
    }

    private int getCycleCount(Stats.Execution record) {
        try {
            var field = record.getClass().getDeclaredField("cycleCount");
            field.setAccessible(true);
            return field.getInt(record);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getOutputValue(Stats.Execution record) {
        try {
            var field = record.getClass().getDeclaredField("outputVariable");
            field.setAccessible(true);
            OutputVariable outVar = (OutputVariable) field.get(record);
            return outVar != null ? outVar.getName() + " = " + outVar.getValue() : "";
        } catch (Exception e) {
            return "";
        }
    }

    public class ExecutionRecord {
        private final int executionNumber;
        private final int expansionLevel;
        private final int cycles;
        private final String output;

        public ExecutionRecord(int executionNumber, int expansionLevel, int cycles, String output) {
            this.executionNumber = executionNumber;
            this.expansionLevel = expansionLevel;
            this.cycles = cycles;
            this.output = output;
        }

        public int getExecutionNumber() { return executionNumber; }
        public int getExpansionLevel() { return expansionLevel; }
        public int getCycles() { return cycles; }
        public String getOutput() { return output; }
    }
}


