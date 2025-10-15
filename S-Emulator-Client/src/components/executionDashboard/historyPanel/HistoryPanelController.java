// components/executionDashboard/historyPanel/HistoryPanelController.java
package components.executionDashboard.historyPanel;

import api.dto.HistoryChainDTO;
import components.executionDashboard.ExecutionDashboardController;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class HistoryPanelController {

    @FXML private TableColumn<HistoryChainDTO, Number> idColumn;
    @FXML private TableColumn<HistoryChainDTO, String> typeColumn;
    @FXML private TableColumn<HistoryChainDTO, Number> cyclesColumn;
    @FXML private TableColumn<HistoryChainDTO, String> labelColumn;
    @FXML private TableView<HistoryChainDTO> historyTable;
    @FXML private TableColumn<HistoryChainDTO, String> instructionColumn;

    private ExecutionDashboardController parent;

    public void setMainController(ExecutionDashboardController parent) {
        this.parent = parent;
    }

    @FXML
    private void initialize() {
        if (historyTable != null) {
            // Set up all columns to display HistoryChainDTO data
            if (idColumn != null) {
                idColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getCommandId()));
            }
            if (typeColumn != null) {
                typeColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getCommandType()));
            }
            if (cyclesColumn != null) {
                cyclesColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getCycles()));
            }
            if (labelColumn != null) {
                labelColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getLabel() != null ? cd.getValue().getLabel() : ""));
            }
            if (instructionColumn != null) {
                instructionColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getInstructionText()));
            }
        }
    }

    /**
     * Update the history table with the parent command chain
     */
    public void setHistoryChain(List<HistoryChainDTO> historyChain) {
        if (historyTable == null) return;
        historyTable.setItems(FXCollections.observableArrayList(historyChain == null ? List.of() : historyChain));
        historyTable.refresh();
    }

    /**
     * Deprecated: kept for backward compatibility with trace lines
     */
    @Deprecated
    public void setTraceLines(List<String> lines) {
        // Convert strings to simple HistoryChainDTO objects for backward compatibility
        if (historyTable == null) return;

        if (lines == null || lines.isEmpty()) {
            historyTable.setItems(FXCollections.observableArrayList());
        } else {
            List<HistoryChainDTO> dtos = new java.util.ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                HistoryChainDTO dto = new HistoryChainDTO();
                dto.setStepNumber(i + 1);
                dto.setCommandName(lines.get(i));
                dto.setArguments("");
                dto.setExpandedFrom("");
                dto.setLevel(0);
                dtos.add(dto);
            }
            historyTable.setItems(FXCollections.observableArrayList(dtos));
        }
        historyTable.refresh();
    }
}
