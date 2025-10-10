package components.mainDashboard.ProgramsAndFunctions;

import components.executionDashboard.ExecutionDashboardController;
import components.mainDashboard.clientMainController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class ProgramsAndFunctionsController {
    public TableColumn programNameColumn;
    public TableColumn uploaderNameColumn;
    public TableColumn numOfInstructionsColumn;
    public TableColumn maxDegreeColumn;
    public TableColumn numOfExecutionsColumn;
    public TableColumn avgCreditCostColumn;
    public Button executeProgramButton;
    public TableColumn functionNameColumn;
    public TableColumn associatedProgramColumn;
    public TableColumn associatedUserColumn;
    public TableColumn numOfInstructionsInFunctionColumn;
    public TableColumn maxDegreeForFunctionColumn;
    public Button executeFunctionButton;
    clientMainController clientMainController;

    public void setMainController(clientMainController mainController) {
        this.clientMainController = mainController;
    }

    public void executeProgramButtonPressed(ActionEvent actionEvent) throws IOException {
        // Load the execution dashboard FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/executionDashboard/executionDashboard.fxml"));
        Parent executionDashboardRoot = loader.load();

        // Get the current stage from the button (similar to your working pattern)
        Stage stage = (Stage) executeProgramButton.getScene().getWindow();

        // Apply the same transition pattern that works in login
        if (stage != null && executionDashboardRoot != null) {
            if (stage.getScene() == null) {
                stage.setScene(new Scene(executionDashboardRoot));
            } else {
                stage.getScene().setRoot(executionDashboardRoot);
            }
            stage.setMaximized(true);
            stage.centerOnScreen();
        }

        // Set the title for the execution dashboard
        stage.setTitle("S-Emulator - Execution Dashboard");
    }

    public void executeFunctionButtonPressed(ActionEvent e) throws IOException {
        // Load the execution dashboard FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/executionDashboard/executionDashboard.fxml"));
        Parent executionDashboardRoot = loader.load();

        // Get the current stage from the button (similar to your working pattern)
        Stage stage = (Stage) executeFunctionButton.getScene().getWindow();

        // Apply the same transition pattern that works in login
        if (stage != null && executionDashboardRoot != null) {
            if (stage.getScene() == null) {
                stage.setScene(new Scene(executionDashboardRoot));
            } else {
                stage.getScene().setRoot(executionDashboardRoot);
            }
            stage.setMaximized(true);
            stage.centerOnScreen();
        }

        // Set the title for the execution dashboard
        stage.setTitle("S-Emulator - Execution Dashboard");
    }

}
