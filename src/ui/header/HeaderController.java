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
import java.util.List;
import java.util.Set;

public class HeaderController {
    public TextField currentDegreeTextField;
    @FXML private ComboBox themeSelector;
    @FXML private ComboBox FunctionAndProgramSelector;
    private BaseController mainController;
    @FXML private Button loadFileButton;
    @FXML private TextField filePathTextBox;
    @FXML public Button CollapseButton;
    @FXML public Button ExpandButton;
    @FXML public Label DegreeLabel;
    @FXML public ComboBox highLightSelector;

    public Object getSelectedFunction() {
        return FunctionAndProgramSelector.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void initialize() {
        themeSelector.getItems().addAll("Default", "Dark", "Blue");
        themeSelector.getSelectionModel().selectFirst();
        themeSelector.setOnAction(e -> {
            if (mainController != null) {
                mainController.switchTheme((String) themeSelector.getValue());
            }
        });
    }

    public void setMainController(BaseController mainController) {
        this.mainController = mainController;
        highLightSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (mainController != null) {
                mainController.onHighlightSelectionChanged(newVal);
            }
        });
        FunctionAndProgramSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (mainController != null && newVal != null) {
                mainController.onFunctionSelectionChanged(newVal.toString());
            }
        });
        // Java
        FunctionAndProgramSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (mainController != null && newVal != null) {
                mainController.onFunctionSelectionChanged(newVal.toString());
                DegreeLabel.setText("/" + mainController.getMaxDegree());
                this.currentDegreeTextField.setText(mainController.getCurrentDegree());
                this.mainController.newRunButtonPressed();
            }
        });
    }

    public void loadFileButtonPressed(ActionEvent actionEvent) throws InterruptedException {
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

        DegreeLabel.setText("/" + mainController.getMaxDegree());
        this.currentDegreeTextField.setText(mainController.getCurrentDegree());
    }

    public void CollapseProgram(ActionEvent actionEvent) {
        Object selected = FunctionAndProgramSelector.getSelectionModel().getSelectedItem();
        if (selected != null) {
            mainController.collapseProgram(selected.toString());
            this.currentDegreeTextField.setText(mainController.getCurrentDegree());
            DegreeLabel.setText("/" + mainController.getMaxDegree());
        }
    }

    public void ExpandProgram(ActionEvent actionEvent) {
        Object selected = FunctionAndProgramSelector.getSelectionModel().getSelectedItem();
        if (selected != null) {
            mainController.expandProgram(selected.toString());
            this.currentDegreeTextField.setText(mainController.getCurrentDegree());
            DegreeLabel.setText("/" + mainController.getMaxDegree());
        }
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

    public void updateFunctionSelector(List<String> functionNames) {
        FunctionAndProgramSelector.getItems().clear();
        FunctionAndProgramSelector.getItems().addAll(functionNames);
        FunctionAndProgramSelector.getSelectionModel().selectFirst();
    }

    public void degreeInserted(ActionEvent actionEvent) {
        if(!mainController.isFileLoaded()) { currentDegreeTextField.clear();return;}
        String degreeText = currentDegreeTextField.getText();
        try {
            int degree = Integer.parseInt(degreeText);
            int maxDegree = Integer.parseInt(mainController.getMaxDegree());
            if (degree >= 0 && degree <= maxDegree) {
                Object selected = FunctionAndProgramSelector.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    mainController.setCurrentDegree(selected.toString(), degree);
                    this.currentDegreeTextField.setText(mainController.getCurrentDegree());
                }
            } else {
                // Reset to current degree if out of bounds
                this.currentDegreeTextField.setText(mainController.getCurrentDegree());
            }
        } catch (NumberFormatException e) {
            // Reset to current degree if input is invalid
            this.currentDegreeTextField.setText(mainController.getCurrentDegree());
        }
    }
}


