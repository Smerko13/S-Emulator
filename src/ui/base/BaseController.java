package ui.base;

import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import ui.header.HeaderController;

public class BaseController {

    @FXML private HeaderController headerComponentController;

    @FXML
    public void initialize() {
        if (headerComponentController != null) {
            headerComponentController.setMainController(this);
        }
    }

    public void setHeaderComponentController(HeaderController headerComponentController) {
        this.headerComponentController = headerComponentController;
        headerComponentController.setMainController(this);
    }



}
