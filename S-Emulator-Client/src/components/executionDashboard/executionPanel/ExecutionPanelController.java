// components/executionDashboard/executionPanel/ExecutionPanelController.java
package components.executionDashboard.executionPanel;

import api.dto.VariableDTO;
import components.executionDashboard.ExecutionDashboardController;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ExecutionPanelController {

    @FXML private Button backToMainDashBoardButton;
    @FXML private ComboBox architectureComboBox;
    @FXML private Button newRunButton, stepOverButton, continueButton, stopDebugButton, startDebugButton, programExecuteButton;
    @FXML private TableView<VariableDTO> inputVarsTable;
    @FXML private TableView<VariableDTO> allVarsTable;
    @FXML private Label cyclesLabel;

    private ExecutionDashboardController parent;

    public void setVariables(java.util.List<VariableDTO> allVars,
                             java.util.List<VariableDTO> inputVars,
                             java.util.Set<String> changedNames) {
        // Left table: all/work/output variables
        var all = javafx.collections.FXCollections.observableArrayList(allVars);
        allVarsTable.setItems(all);

        // Right table: inputs
        var inputs = javafx.collections.FXCollections.observableArrayList(inputVars);
        inputVarsTable.setItems(inputs);

        // simple highlight for changed names
        allVarsTable.setRowFactory(tv -> new TableRow<VariableDTO>() {
            @Override protected void updateItem(VariableDTO item, boolean empty) {
                super.updateItem(item, empty);
                boolean changed = !empty && item != null && changedNames != null
                        && changedNames.contains(item.getName());
                setStyle(changed ? "-fx-background-color: lightgreen;" : "");
            }
        });
        inputVarsTable.setRowFactory(tv -> new TableRow<VariableDTO>() {
            @Override protected void updateItem(VariableDTO item, boolean empty) {
                super.updateItem(item, empty);
                boolean changed = !empty && item != null && changedNames != null
                        && changedNames.contains(item.getName());
                setStyle(changed ? "-fx-background-color: lightgreen;" : "");
            }
        });

        // column factories — run once if you haven’t already:
        if (allVarsTable.getColumns().size() == 2) {
            @SuppressWarnings("unchecked")
            TableColumn<VariableDTO, String> nameCol =
                    (TableColumn<VariableDTO, String>) allVarsTable.getColumns().get(0);
            @SuppressWarnings("unchecked")
            TableColumn<VariableDTO, Number> valueCol =
                    (TableColumn<VariableDTO, Number>) allVarsTable.getColumns().get(1);
            nameCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
            valueCol.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getValue()));
        }
        if (inputVarsTable.getColumns().size() == 2) {
            @SuppressWarnings("unchecked")
            TableColumn<VariableDTO, String> nameCol =
                    (TableColumn<VariableDTO, String>) inputVarsTable.getColumns().get(0);
            @SuppressWarnings("unchecked")
            TableColumn<VariableDTO, Number> valueCol =
                    (TableColumn<VariableDTO, Number>) inputVarsTable.getColumns().get(1);
            nameCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
            valueCol.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getValue()));
        }

        allVarsTable.refresh();
        inputVarsTable.refresh();
    }


    public void setMainController(ExecutionDashboardController parent) {
        this.parent = parent;
    }



    // Must be public (dashboard calls it)
    public void setCyclesLabel(int cycles) {
        if (cyclesLabel != null) cyclesLabel.setText("Cycles: " + cycles);
    }

    // New signature used by dashboard
    public void updateDebugButtons(boolean debugging) {
        stepOverButton.setDisable(!debugging);
        continueButton.setDisable(!debugging);
        stopDebugButton.setDisable(!debugging);
        // start/execute/newRun can remain enabled as you prefer
    }

    public void executeButtonPressed(ActionEvent actionEvent) {
    }

    public void debugButtonPressed(ActionEvent actionEvent) {
    }

    public void newRunButtonPressed(ActionEvent actionEvent) {
    }

    public void stepOverPressed(ActionEvent actionEvent) {
    }

    public void stopDebugPressed(ActionEvent actionEvent) {
    }

    public void continueButtonPressed(ActionEvent actionEvent) {
    }

    public void backToMainDashBoard(ActionEvent actionEvent) {
        try {
            // Load the main dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/mainDashboard/mainDashboard.fxml"));
            Parent mainDashboardRoot = loader.load();

            // Get the current stage from the button (using your proven pattern)
            Stage stage = (Stage) backToMainDashBoardButton.getScene().getWindow();

            // Apply the same transition pattern that works well
            if (stage != null && mainDashboardRoot != null) {
                if (stage.getScene() == null) {
                    stage.setScene(new Scene(mainDashboardRoot));
                } else {
                    stage.getScene().setRoot(mainDashboardRoot);
                }
                stage.setMaximized(true);
                stage.centerOnScreen();
            }

            // Set the title for the main dashboard
            stage.setTitle("S-Emulator - Main Dashboard");

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error gracefully - could show an alert dialog here if needed
        }
    }
}
