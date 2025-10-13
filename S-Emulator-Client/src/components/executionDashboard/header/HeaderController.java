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
    @FXML private TextField currentDegreeTextField;
    @FXML private Label DegreeLabel;
    @FXML private Button ExpandButton;

    private ExecutionDashboardController parent;   // <-- replace BaseController
    private final UserSession userSession;
    private String currentProgramName; // Store the current program name

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
        // No longer needed, as we are not using FunctionAndProgramSelector
    }

    // existing button handlers can delegate to parent
    @FXML private void CollapseProgram() {
        if (parent != null && currentProgramName != null) {
            parent.collapseProgram(currentProgramName);
        }
    }

    @FXML private void ExpandProgram() {
        if (parent != null && currentProgramName != null) {
            parent.expandProgram(currentProgramName);
        }
    }

    @FXML private void degreeInserted() {
        if (parent == null || currentDegreeTextField == null || currentProgramName == null) return;
        try {
            int v = Integer.parseInt(currentDegreeTextField.getText().trim());
            parent.setCurrentDegree(currentProgramName, v);
        } catch (NumberFormatException ignored) {
            // Reset to current value if invalid input
            // This will be updated when the server responds
        }
    }

    // helper the dashboard calls
    public String getSelectedFunction() {
        return currentProgramName;
    }

    public void setSelectedFunction(String name) {
        this.currentProgramName = name;
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
