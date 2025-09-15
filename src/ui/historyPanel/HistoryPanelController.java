package ui.historyPanel;

import javafx.fxml.FXML;
import javafx.scene.text.TextFlow;
import ui.base.BaseController;

public class HistoryPanelController {
    @FXML
    private TextFlow TextFlow;
    private BaseController mainController;

    public void setMainController(BaseController baseController) {
        this.mainController = baseController;
    }
}
