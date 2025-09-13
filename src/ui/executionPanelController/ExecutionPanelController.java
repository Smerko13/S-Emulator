package ui.executionPanelController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import ui.base.BaseController;

public class ExecutionPanelController {


    @FXML private TableView inputVarsTable;
    @FXML private TableView allVarsTable;
    @FXML private Button continueButton;
    @FXML private Button stopDebugButton;
    @FXML private Button startDebugButton;
    @FXML private Button programExecuteButton;
    @FXML private Label cyclesLabel;
    private BaseController mainController;

    public void setMainController(BaseController mainController) {
        this.mainController = mainController;
    }

    public void executeButtonPressed(ActionEvent actionEvent) {
    }

    public void debugButtonPressed(ActionEvent actionEvent) {
    }

    public void stopDebugPressed(ActionEvent actionEvent) {
    }

    public void continueButtonPressed(ActionEvent actionEvent) {
    }
}
