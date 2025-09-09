package ui.header;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import ui.base.BaseController;

import java.io.File;

public class HeaderController {
    private BaseController mainController;
    @FXML private Button loadFileButton;
    @FXML private TextField filePathTextBox;


    public void setMainController(BaseController mainController) {
        this.mainController = mainController;
    }


    public void loadFileButtonPressed(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Program");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File selectedFile = fileChooser.showOpenDialog(loadFileButton.getScene().getWindow());
        if (selectedFile != null) {
            filePathTextBox.setText(selectedFile.getAbsolutePath());
        }
    }
}


