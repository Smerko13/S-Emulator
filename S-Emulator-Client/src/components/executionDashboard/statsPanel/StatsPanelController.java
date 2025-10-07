package components.executionDashboard.statsPanel;

import api.dto.StatsDTO;
import api.dto.VariableDTO;
import components.executionDashboard.ExecutionDashboardController;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class StatsPanelController {

    @FXML private TableView<ExecutionRecord> statsTable;
    @FXML private TableColumn<ExecutionRecord, Number> executionNumberColumn;
    @FXML private TableColumn<ExecutionRecord, Number> expansionLevelColumn;
    @FXML private TableColumn<ExecutionRecord, Number> cyclesColumn;
    @FXML private TableColumn<ExecutionRecord, String> outputColumn;
    @FXML private Button showStatusButton;
    @FXML private Button reRunButton;

    private ExecutionDashboardController parent;

    private final ObservableList<ExecutionRecord> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        statsTable.setItems(rows);
        executionNumberColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().executionNumber));
        expansionLevelColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().expansionLevel));
        cyclesColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().cycles));
        outputColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().outputText));
    }

    /** Called by ExecutionDashboardController with the server DTO */
    public void updateStats(StatsDTO dto) {
        rows.clear();
        if (dto == null || dto.getExecutions() == null) return;

        for (StatsDTO.Execution r : dto.getExecutions()) {
            rows.add(new ExecutionRecord(
                    r.getExecutionNumber(),
                    r.getExpansionLevel(),
                    r.getCycles(),
                    r.getOutputText(),
                    r.getInputVars()
            ));
        }
    }

    // --- Button stubs (keep FXML happy). Wire to server later if you want.
    @FXML
    private void showStatusButtonPressed() {
        // Optional: ask the dashboard to request a “status for execution #N” from the server.
        // Left empty for now.
    }

    @FXML
    private void reRunButtonPressed() {
        // Optional: ask the dashboard to re-run with inputs from the selected row.
        // Left empty for now.
    }

    public void setMainController(ExecutionDashboardController executionDashboardController) {
        this.parent = executionDashboardController;
    }

    /** Row model for the table */
    public static class ExecutionRecord {
        final int executionNumber;
        final int expansionLevel;
        final int cycles;
        final String outputText;
        final List<VariableDTO> inputVars;

        public ExecutionRecord(int executionNumber,
                               int expansionLevel,
                               int cycles,
                               String outputText,
                               List<VariableDTO> inputVars) {
            this.executionNumber = executionNumber;
            this.expansionLevel = expansionLevel;
            this.cycles = cycles;
            this.outputText = outputText;
            this.inputVars = inputVars;
        }

        // getters are optional because we bind via lambdas, but add if you like
        public int getExecutionNumber() { return executionNumber; }
        public int getExpansionLevel()  { return expansionLevel;  }
        public int getCycles()          { return cycles;          }
        public String getOutputText()   { return outputText;      }
        public List<VariableDTO> getInputVars() { return inputVars; }
    }
}
