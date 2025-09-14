package ui.executionPanelController;

import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import ui.base.BaseController;

import java.util.Set;

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
        this.mainController.executeProgram();
    }

    public void debugButtonPressed(ActionEvent actionEvent) {
    }

    public void stopDebugPressed(ActionEvent actionEvent) {
    }

    public void continueButtonPressed(ActionEvent actionEvent) {
    }

    public void displayAllVars(Set<Variable> variables) {
        allVarsTable.getItems().clear();
        ObservableList<Variable> allVars = FXCollections.observableArrayList();
        for (Variable v : variables) {
            if (v instanceof WorkVariable || v instanceof OutputVariable) {
                allVars.add(v);
            }
        }
        allVarsTable.setItems(allVars);

        // Optional: Set up columns if not already done in FXML/controller
        if (allVarsTable.getColumns().size() == 2) {
            TableColumn<Variable, String> nameCol = (TableColumn<Variable, String>) allVarsTable.getColumns().get(0);
            TableColumn<Variable, Integer> valueCol = (TableColumn<Variable, Integer>) allVarsTable.getColumns().get(1);
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        }
    }

    public void displayInputVars(Set<Variable> variables) {
        inputVarsTable.getItems().clear();
        ObservableList<InputVariable> inputVars = FXCollections.observableArrayList();
        for (Variable v : variables) {
            if (v instanceof InputVariable) {
                inputVars.add((InputVariable) v);
            }
        }
        inputVarsTable.setItems(inputVars);

        if (inputVarsTable.getColumns().size() == 2) {
            TableColumn<InputVariable, String> nameCol = (TableColumn<InputVariable, String>) inputVarsTable.getColumns().get(0);
            TableColumn<InputVariable, Integer> valueCol = (TableColumn<InputVariable, Integer>) inputVarsTable.getColumns().get(1);

            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

            valueCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            valueCol.setOnEditCommit(event -> {
                InputVariable var = event.getRowValue();
                var.setValue(event.getNewValue());
            });

            inputVarsTable.setEditable(true);
        }
    }
}
