package ui.statPanel;

import engine.Stats;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import ui.base.BaseController;

public class StatPanelController {
    @FXML
    public ComboBox<Integer> runNumbersBox;
    @FXML
    public TextFlow historyDetailsBox;
    public Button showHistoryButton;
    public Button reRunButton;
    private BaseController mainController;

    public void setMainController(BaseController mainController) {
        this.mainController = mainController;
    }

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

    @FXML
    public void showSelectedHistory(ActionEvent actionEvent) {
        historyDetailsBox.getChildren().clear();
        Integer selected = runNumbersBox.getSelectionModel().getSelectedItem();
        if (selected == null || mainController == null) return;
        Stats stats = mainController.getStats();
        if (stats == null || stats.getExecutionHistory().size() < selected) return;
        var execution = stats.getExecutionHistory().get(selected - 1);
        historyDetailsBox.getChildren().add(new Text(execution.toString()));
    }

    public void reRunProgram(ActionEvent actionEvent) {
    }
}
