package components.mainDashboard.ProgramsAndFunctions;

import components.executionDashboard.ExecutionDashboardController;
import components.mainDashboard.clientMainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
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

        Stage stage = new Stage();
        stage.setTitle("Execution");
        stage.setScene(new Scene(root));

// 3/4 of the screen, centered
        javafx.geometry.Rectangle2D vb = javafx.stage.Screen.getPrimary().getVisualBounds();
        double w = vb.getWidth()  * 0.75;
        double h = vb.getHeight() * 0.75;
        stage.setWidth(w);
        stage.setHeight(h);
        stage.setX(vb.getMinX() + (vb.getWidth()  - w) / 2);
        stage.setY(vb.getMinY() + (vb.getHeight() - h) / 2);

// (optional) reasonable mins so it's still usable if resized smaller
        stage.setMinWidth(vb.getWidth() * 0.5);
        stage.setMinHeight(vb.getHeight() * 0.5);

        stage.show();

        ctrl.openOnServer(target);

    }

    public void executeFunctionButtonPressed(ActionEvent actionEvent) {
    }
}
