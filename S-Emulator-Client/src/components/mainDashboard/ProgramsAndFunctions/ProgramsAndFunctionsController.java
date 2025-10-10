package components.mainDashboard.ProgramsAndFunctions;

import components.executionDashboard.ExecutionDashboardController;
import components.mainDashboard.clientMainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
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
        String target = "test";
        FXMLLoader fxml = new FXMLLoader(
                getClass().getResource("/components/executionDashboard/executionDashboard.fxml"));
        Parent root = fxml.load();
        ExecutionDashboardController ctrl = fxml.getController();

        // Switch scene on current stage
        Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        stage.setTitle("Execution");
        stage.setResizable(true);
        stage.setScene(new Scene(root));

        stage.show();

        Screen screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1)
                .stream().findFirst().orElse(Screen.getPrimary());
        Rectangle2D vb = screen.getVisualBounds();
        stage.setX(vb.getMinX());
        stage.setY(vb.getMinY());
        stage.setWidth(vb.getWidth());
        stage.setHeight(vb.getHeight());

        ctrl.openOnServer(target);
    }

    public void executeFunctionButtonPressed(ActionEvent actionEvent) throws IOException {
        String target = "functionTest";
        FXMLLoader fxml = new FXMLLoader(
                getClass().getResource("/components/executionDashboard/executionDashboard.fxml"));
        Parent root = fxml.load();
        ExecutionDashboardController ctrl = fxml.getController();

        Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        stage.setTitle("Execution");
        stage.setResizable(true);
        stage.setScene(new Scene(root));

        // Show first so window decorations are known
        stage.show();

        // Then size the *stage* (not the scene) to the screen's visual area (no taskbar overlap)
        Screen screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1)
                .stream().findFirst().orElse(Screen.getPrimary());
        Rectangle2D vb = screen.getVisualBounds();
        stage.setX(vb.getMinX());
        stage.setY(vb.getMinY());
        stage.setWidth(vb.getWidth());
        stage.setHeight(vb.getHeight());

        // no setMaximized() needed
        ctrl.openOnServer("functionTest");
    }


}
