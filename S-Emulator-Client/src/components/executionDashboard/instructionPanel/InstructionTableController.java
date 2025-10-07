package components.executionDashboard.instructionPanel;

import api.dto.InstructionDTO;
import components.executionDashboard.ExecutionDashboardController;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;

public class InstructionTableController {

    @FXML private TableView<InstructionDTO> instructionTableView;
    @FXML private TableColumn<InstructionDTO, Number> idColumn;
    @FXML private TableColumn<InstructionDTO, String> typeColumn;
    @FXML private TableColumn<InstructionDTO, Number> cyclesColumn;
    @FXML private TableColumn<InstructionDTO, String> labelColumn;
    @FXML private TableColumn<InstructionDTO, String> instructionColumn;

    @FXML private TextFlow SummaryLineTextBox;

    private ExecutionDashboardController parent;

    private final ObservableList<InstructionDTO> rows = FXCollections.observableArrayList();
    private Integer debugHighlightId = null;

    @FXML
    private void initialize() {
        instructionTableView.setItems(rows);

        // Use DTO fields (no getIndex() here)
        idColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getId()));
        typeColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getType()));
        cyclesColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getCycles()));
        labelColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getLabel() == null ? "" : cd.getValue().getLabel()));
        instructionColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getText()));

        // Row highlighting for the debug instruction id (if server provides it)
        instructionTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InstructionDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (debugHighlightId != null && item.getId() == debugHighlightId) {
                    setStyle("-fx-background-color: red;");
                } else {
                    setStyle("");
                }
            }
        });

        // Show a small summary of the selected instruction
        instructionTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, cur) -> {
            SummaryLineTextBox.getChildren().clear();
            if (cur != null) {
                SummaryLineTextBox.getChildren().add(new Text(
                        cur.getText() + "\n" +
                                "Type: " + cur.getType() + "\n" +
                                "Cycles: " + cur.getCycles() + "\n" +
                                "Label: " + (cur.getLabel() == null ? "" : cur.getLabel())
                ));
            }
        });
    }

    /** Called by ExecutionDashboardController */
    public void setInstructions(List<InstructionDTO> list, Integer highlightedId) {
        rows.setAll(list == null ? List.of() : list);
        this.debugHighlightId = highlightedId;
        instructionTableView.refresh();
    }

    // Optional: wired to "Show Program Summary" button if you still have it in FXML
    public void ShowProgramSummary() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Program Summary");
        a.setHeaderText("Program Summary");
        a.setContentText("Summary is now provided by the server-side panel; this button is optional.");
        a.showAndWait();
    }

    public void setMainController(ExecutionDashboardController executionDashboardController) {
        parent = executionDashboardController;
    }
}
