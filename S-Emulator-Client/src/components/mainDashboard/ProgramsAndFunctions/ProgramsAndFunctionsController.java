package components.mainDashboard.ProgramsAndFunctions;

import components.clientMainController;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;

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

    public void executeProgramButtonPressed(ActionEvent actionEvent) {
    }

    public void executeFunctionButtonPressed(ActionEvent actionEvent) {
    }
}
