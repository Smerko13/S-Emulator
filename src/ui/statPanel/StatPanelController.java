package ui.statPanel;

import engine.Stats;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.TextFlow;
import ui.base.BaseController;

public class StatPanelController {
    @FXML
    public ComboBox<Integer> runNumbersBox;
    @FXML
    public TextFlow historyDetailsBox;
    private BaseController mainController;

    public void setMainController(BaseController mainController) {
        this.mainController = mainController;
    }

    // Call this after each execution to sync with engine stats
    public void refreshExecutionNumbers(Stats stats) {
        runNumbersBox.getItems().clear();
        int count = stats == null ? 0 : stats.getExecutionHistory().size();
        for (int i = 1; i <= count; i++) {
            runNumbersBox.getItems().add(i);
        }
        if (count > 0) {
            runNumbersBox.getSelectionModel().selectLast();
        }
    }
}
