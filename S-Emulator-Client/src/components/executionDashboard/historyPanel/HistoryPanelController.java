// components/executionDashboard/historyPanel/HistoryPanelController.java
package components.executionDashboard.historyPanel;

import components.executionDashboard.ExecutionDashboardController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class HistoryPanelController {

    @FXML private TableColumn idColumn;
    @FXML private TableColumn typeColumn;
    @FXML private TableColumn cyclesColumn;
    @FXML private TableColumn labelColumn;
    @FXML private TableView<String> historyTable;     // simplest: swap to String rows
    @FXML private TableColumn<String, String> instructionColumn;

    private ExecutionDashboardController parent;

    public void setMainController(ExecutionDashboardController parent) {
        this.parent = parent;
    }

    @FXML
    private void initialize() {
        if (historyTable != null && instructionColumn != null) {
            // Set up the single column to display the command strings
            instructionColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue()));
        }
    }

    // DTO-era method the dashboard calls
    public void setTraceLines(List<String> lines) {
        if (historyTable == null) return;
        historyTable.setItems(FXCollections.observableArrayList(lines == null ? List.of() : lines));
        historyTable.refresh();
    }

}
