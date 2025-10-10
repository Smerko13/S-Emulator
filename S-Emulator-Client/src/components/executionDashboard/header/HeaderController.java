// components/executionDashboard/header/HeaderController.java
package components.executionDashboard.header;

import components.executionDashboard.ExecutionDashboardController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class HeaderController {
    @FXML private ComboBox highLightSelector;
    @FXML private Label nameLabel;
    @FXML private Button CollapseButton;            // already in FXML
    @FXML private Label creditsLabel;
    @FXML private Label userNameLabel;
    @FXML private ComboBox<String> FunctionAndProgramSelector;
    @FXML private TextField currentDegreeTextField;
    @FXML private Label DegreeLabel;
    @FXML private Button ExpandButton;

    private ExecutionDashboardController parent;   // <-- replace BaseController

    public void setMainController(ExecutionDashboardController parent) {
        this.parent = parent;
    }

    // called by dashboard when a fresh state arrives
    public void updateFunctionSelector(List<String> names) {
        FunctionAndProgramSelector.getItems().setAll(names == null ? List.of() : names);
    }

    // existing button handlers can delegate to parent
    @FXML private void CollapseProgram() {
        if (parent != null && FunctionAndProgramSelector.getValue() != null)
            parent.collapseProgram(FunctionAndProgramSelector.getValue());
    }
    @FXML private void ExpandProgram() {
        if (parent != null && FunctionAndProgramSelector.getValue() != null)
            parent.expandProgram(FunctionAndProgramSelector.getValue());
    }
    @FXML private void degreeInserted() {
        if (parent == null) return;
        String sel = FunctionAndProgramSelector.getValue();
        if (sel == null) return;
        try {
            int v = Integer.parseInt(currentDegreeTextField.getText().trim());
            parent.setCurrentDegree(sel, v);
        } catch (NumberFormatException ignored) { }
    }

    // helper the dashboard calls
    public Object getSelectedFunction() { return FunctionAndProgramSelector.getValue(); }

    public void setSelectedFunction(String name) {
        if (name == null) return;
        FunctionAndProgramSelector.getSelectionModel().select(name);
    }

    public void setDegreeLabels(int current, int max) {
        currentDegreeTextField.setText(String.valueOf(current));
        DegreeLabel.setText("/" + max);
    }

    public void loadFileButtonPressed(ActionEvent actionEvent) {
    }
}
