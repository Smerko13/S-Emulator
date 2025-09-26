package ui.executionPanelController;

import engine.arguments.Variable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import ui.base.BaseController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExecutionPanelController {
    @FXML private Button newRunButton;
    @FXML private Button stepOverButton;
    @FXML private TableView inputVarsTable;
    @FXML private TableView allVarsTable;
    @FXML private Button continueButton;
    @FXML private Button stopDebugButton;
    @FXML private Button startDebugButton;
    @FXML private Button programExecuteButton;
    @FXML private Label cyclesLabel;
    private BaseController mainController;
    private Set<String> changedVarNames = new HashSet<>();

    public void initialize() {
        disableAllButtons();
        inputVarsTable.setEditable(true); // Allow editing
        if (inputVarsTable.getColumns().size() == 2) {
            TableColumn<Variable, String> nameCol = (TableColumn<Variable, String>) inputVarsTable.getColumns().get(0);
            TableColumn<Variable, Integer> valueCol = (TableColumn<Variable, Integer>) inputVarsTable.getColumns().get(1);
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            valueCol.setOnEditCommit(event -> {
                Variable var = event.getRowValue();
                if (var instanceof engine.arguments.types.InputVariable) {
                    var.setValue(event.getNewValue());
                }
            });
        }
    }

    private void disableAllButtons() {
        stepOverButton.setDisable(true);
        stepOverButton.setOpacity(0.5); // Dim when disabled
        continueButton.setDisable(true);
        continueButton.setOpacity(0.5); // Dim when disabled
        stopDebugButton.setDisable(true);
        stopDebugButton.setOpacity(0.5); // Dim when disabled
        startDebugButton.setDisable(true);
        startDebugButton.setOpacity(0.5); // Dim when disabled
        programExecuteButton.setDisable(true);
        programExecuteButton.setOpacity(0.5); // Dim when disabled
    }

    public void enableAllButtons() {
        stepOverButton.setDisable(false);
        stepOverButton.setOpacity(1.0); // Full opacity when enabled
        continueButton.setDisable(false);
        continueButton.setOpacity(1.0); // Full opacity when enabled
        stopDebugButton.setDisable(false);
        stopDebugButton.setOpacity(1.0); // Full opacity when enabled
        startDebugButton.setDisable(false);
        startDebugButton.setOpacity(1.0); // Full opacity when enabled
        programExecuteButton.setDisable(false);
        programExecuteButton.setOpacity(1.0); // Full opacity when enabled
    }

    public void setMainController(BaseController mainController) {
        this.mainController = mainController;
    }

    public void executeButtonPressed(ActionEvent actionEvent) {
        this.mainController.executeProgram();
    }

    public void debugButtonPressed(ActionEvent actionEvent) {
        this.mainController.startDebugging();
    }

    public void stopDebugPressed(ActionEvent actionEvent) {
        this.mainController.stopDebugging();

    }

    public void continueButtonPressed(ActionEvent actionEvent) {
        this.mainController.continueDebugging();
    }

    public void displayAllVars(Set<Variable> variables) {
        allVarsTable.getItems().clear();
        ObservableList<Variable> allVars = FXCollections.observableArrayList();
        for (Variable v : variables) {
            if (v instanceof WorkVariable || v instanceof OutputVariable) {
                allVars.add(v); // Only current values are in the variable objects
            }
        }
        allVarsTable.setItems(allVars);

        if (allVarsTable.getColumns().size() == 2) {
            TableColumn<Variable, String> nameCol = (TableColumn<Variable, String>) allVarsTable.getColumns().get(0);
            TableColumn<Variable, Integer> valueCol = (TableColumn<Variable, Integer>) allVarsTable.getColumns().get(1);
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        }
    }

    public void displayAllVars(Set<Variable> variables, Set<String> changedVarNames) {
        this.changedVarNames = changedVarNames != null ? changedVarNames : Set.of();
        allVarsTable.getItems().clear();
        ObservableList<Variable> allVars = FXCollections.observableArrayList();
        for (Variable v : variables) {
            if (v instanceof WorkVariable || v instanceof OutputVariable) {
                allVars.add(v);
            }
        }
        allVarsTable.setItems(allVars);

        if (allVarsTable.getColumns().size() == 2) {
            TableColumn<Variable, String> nameCol = (TableColumn<Variable, String>) allVarsTable.getColumns().get(0);
            TableColumn<Variable, Integer> valueCol = (TableColumn<Variable, Integer>) allVarsTable.getColumns().get(1);
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        }
        allVarsTable.setRowFactory(tv -> new TableRow<Variable>() {
            @Override
            protected void updateItem(Variable item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && changedVarNames != null && !changedVarNames.isEmpty() && changedVarNames.contains(item.getName())) {
                    setStyle("-fx-background-color: lightgreen;");
                } else {
                    setStyle("");
                }
            }
        });
        // Remove highlight after 2 seconds
        if (!this.changedVarNames.isEmpty()) {
                    this.changedVarNames = Set.of();
                    allVarsTable.refresh();
        }
        allVarsTable.refresh();
        inputVarsTable.refresh();
    }

    public void displayVarsForCurrentInstructions(Set<Variable> variables, List<Command> currentCommands) {
        // Collect variable names used in current commands
        Set<String> usedVarNames = new HashSet<>();
        for (Command cmd : currentCommands) {
            usedVarNames.addAll(cmd.getUsedVariableNames());
        }

        ObservableList<Variable> filteredVars = FXCollections.observableArrayList();
        for (Variable v : variables) {
            if ((v instanceof WorkVariable || v instanceof OutputVariable) && usedVarNames.contains(v.getName())) {
                filteredVars.add(v);
            }
        }
        allVarsTable.getItems().clear();
        allVarsTable.setItems(filteredVars);

        if (allVarsTable.getColumns().size() == 2) {
            TableColumn<Variable, String> nameCol = (TableColumn<Variable, String>) allVarsTable.getColumns().get(0);
            TableColumn<Variable, Integer> valueCol = (TableColumn<Variable, Integer>) allVarsTable.getColumns().get(1);
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        }
    }

    public void setCyclesLabel(int cycles) {
        cyclesLabel.setText("Cycles: " + cycles);
    }

    public void stepOverPressed(ActionEvent actionEvent) {
        this.mainController.stepOver();
    }

    public void updateDebugButtons() {
        boolean enabled = mainController != null && mainController.isDebuggingEnabled();
        stepOverButton.setDisable(!enabled);
        stepOverButton.setOpacity(enabled ? 1.0 : 0.5); // Dim when disabled
        continueButton.setDisable(!enabled);
        continueButton.setOpacity(enabled ? 1.0 : 0.5); // Dim when disabled
        stopDebugButton.setDisable(!enabled);
        stopDebugButton.setOpacity(enabled ? 1.0 : 0.5); // Dim when disabled
    }

    public void clearAllVars() {
        allVarsTable.getItems().clear();
        inputVarsTable.getItems().clear();
        cyclesLabel.setText("Cycles: 0");
    }

    // Java
    public void displayInputVars(Set<Variable> variables, Set<String> changedVarNames) {
        inputVarsTable.getItems().clear();
        ObservableList<Variable> inputVars = FXCollections.observableArrayList();
        for (Variable v : variables) {
            if (v instanceof engine.arguments.types.InputVariable) {
                inputVars.add(v);
            }
        }
        inputVarsTable.setItems(inputVars);

        if (inputVarsTable.getColumns().size() == 2) {
            TableColumn<Variable, String> nameCol = (TableColumn<Variable, String>) inputVarsTable.getColumns().get(0);
            TableColumn<Variable, Integer> valueCol = (TableColumn<Variable, Integer>) inputVarsTable.getColumns().get(1);
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        }
        inputVarsTable.setRowFactory(tv -> new TableRow<Variable>() {
            @Override
            protected void updateItem(Variable item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && changedVarNames != null && !changedVarNames.isEmpty() && changedVarNames.contains(item.getName())) {
                    setStyle("-fx-background-color: lightgreen;");
                } else {
                    setStyle("");
                }
            }
        });
        // Remove highlight after 2 seconds
        if (changedVarNames != null && !changedVarNames.isEmpty()) {
            inputVarsTable.refresh();
        }
        allVarsTable.refresh();
        inputVarsTable.refresh();
    }

    public void newRunButtonPressed(ActionEvent actionEvent) {
        this.mainController.newRunButtonPressed();
    }
}
