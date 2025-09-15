package ui.header;

import engine.arguments.Variable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import ui.base.BaseController;

import java.io.File;
import java.util.Set;

public class HeaderController {
    private BaseController mainController;
    @FXML private Button loadFileButton;
    @FXML private TextField filePathTextBox;
    @FXML public Button CollapseButton;
    @FXML public Button ExpandButton;
    @FXML public Label DegreeLabel;
    @FXML public ComboBox highLightSelector;


    public void setMainController(BaseController mainController) {
        this.mainController = mainController;
        highLightSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (mainController != null) {
                mainController.onHighlightSelectionChanged(newVal);
            }
        });
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
            mainController.loadFile(selectedFile);
        }

        DegreeLabel.setText("Degree: " + mainController.getCurrentDegree() + "/" + mainController.getMaxDegree());
    }

    public void CollapseProgram(ActionEvent actionEvent) {
        mainController.collapseProgram();
        DegreeLabel.setText("Degree: " + mainController.getCurrentDegree() + "/" + mainController.getMaxDegree());
    }

    public void ExpandProgram(ActionEvent actionEvent) {
        mainController.expandProgram();
        DegreeLabel.setText("Degree: " + mainController.getCurrentDegree() + "/" + mainController.getMaxDegree());
    }

    public void setHeaderMonitors(Set<String> labels, Set<Variable> variables) {
        highLightSelector.getItems().clear();
        highLightSelector.getItems().add("None");
        if(labels != null) {
            highLightSelector.getItems().addAll(labels);
        }
        if(variables != null) {
            for (Variable var : variables) {
                highLightSelector.getItems().add(var.getName());
            }
        }
        highLightSelector.getSelectionModel().selectFirst();

    }


}


