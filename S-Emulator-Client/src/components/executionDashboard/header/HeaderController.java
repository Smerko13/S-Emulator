// components/executionDashboard/header/HeaderController.java
package components.executionDashboard.header;

import components.executionDashboard.ExecutionDashboardController;
import components.shared.UserSession;
import javafx.application.Platform;
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
    private final UserSession userSession;

    public HeaderController() {
        userSession = UserSession.getInstance();
    }

    public void setMainController(ExecutionDashboardController parent) {
        this.parent = parent;

        // Bind UI components to shared session for real-time updates
        userSession.userNameProperty().addListener((obs, oldVal, newVal) ->
            Platform.runLater(() -> {
                if (userNameLabel != null) {
                    userNameLabel.setText("User: " + newVal);
                }
            }));
        userSession.creditsProperty().addListener((obs, oldVal, newVal) ->
            Platform.runLater(() -> {
                if (creditsLabel != null) {
                    creditsLabel.setText("Credits: " + newVal);
                }
            }));

        // Initialize with current values from shared session
        Platform.runLater(() -> {
            if (userNameLabel != null) {
                userNameLabel.setText("User: " + userSession.getUserName());
            }
            if (creditsLabel != null) {
                creditsLabel.setText("Credits: " + userSession.getCredits());
            }
        });
    }

    // called by dashboard when a fresh state arrives
    public void updateFunctionSelector(List<String> names) {
        if (FunctionAndProgramSelector != null) {
            FunctionAndProgramSelector.getItems().setAll(names == null ? List.of() : names);
        }
    }

    // existing button handlers can delegate to parent
    @FXML private void CollapseProgram() {
        if (parent != null && FunctionAndProgramSelector != null && FunctionAndProgramSelector.getValue() != null)
            parent.collapseProgram(FunctionAndProgramSelector.getValue());
    }
    @FXML private void ExpandProgram() {
        if (parent != null && FunctionAndProgramSelector != null && FunctionAndProgramSelector.getValue() != null)
            parent.expandProgram(FunctionAndProgramSelector.getValue());
    }
    @FXML private void degreeInserted() {
        if (parent == null || FunctionAndProgramSelector == null) return;
        String sel = FunctionAndProgramSelector.getValue();
        if (sel == null || currentDegreeTextField == null) return;
        try {
            int v = Integer.parseInt(currentDegreeTextField.getText().trim());
            parent.setCurrentDegree(sel, v);
        } catch (NumberFormatException ignored) { }
    }

    // helper the dashboard calls
    public Object getSelectedFunction() {
        return FunctionAndProgramSelector != null ? FunctionAndProgramSelector.getValue() : null;
    }

    public void setSelectedFunction(String name) {
        if (name == null || FunctionAndProgramSelector == null) return;
        FunctionAndProgramSelector.getSelectionModel().select(name);
    }

    public void setDegreeLabels(int current, int max) {
        if (currentDegreeTextField != null) {
            currentDegreeTextField.setText(String.valueOf(current));
        }
        if (DegreeLabel != null) {
            DegreeLabel.setText("/" + max);
        }
    }

    public void setProgramOrFunctionName(String name) {
        if (nameLabel != null && name != null) {
            nameLabel.setText("Program/Function Name: " + name);
        }
    }

    public void loadFileButtonPressed(ActionEvent actionEvent) {
    }
}
