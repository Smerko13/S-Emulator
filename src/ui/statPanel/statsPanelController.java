package ui.statPanel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class statsPanelController {
    @FXML private TableView statsTable;
    @FXML private TableColumn executionNumberColumn;
    @FXML private TableColumn expansionLevelColumn;
    @FXML private TableColumn cyclesColumn;
    @FXML private TableColumn outputColumn;
    @FXML private Button showStatusButton;
    @FXML private Button reRunButton;

    public void showStatusButtonPressed(ActionEvent actionEvent) {
    }

    public void reRunButtonPressed(ActionEvent actionEvent) {
    }
}
