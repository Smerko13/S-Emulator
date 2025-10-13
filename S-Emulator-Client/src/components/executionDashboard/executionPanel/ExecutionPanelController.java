// components/executionDashboard/executionPanel/ExecutionPanelController.java
package components.executionDashboard.executionPanel;

import api.dto.VariableDTO;
import components.executionDashboard.ExecutionDashboardController;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ExecutionPanelController {

    @FXML private Button backToMainDashBoardButton;
    @FXML private ComboBox architectureComboBox;
    @FXML private Button newRunButton, stepOverButton, continueButton, stopDebugButton, startDebugButton, programExecuteButton;
    @FXML private TableView<VariableDTO> inputVarsTable;
    @FXML private TableView<VariableDTO> allVarsTable;
    @FXML private Label cyclesLabel;

    private ExecutionDashboardController parent;

    public void setVariables(java.util.List<VariableDTO> allVars,
                             java.util.List<VariableDTO> inputVars,
                             java.util.Set<String> changedNames) {
        System.out.println("==================== VARIABLES UPDATE RECEIVED ====================");
        System.out.println("setVariables called - updating UI with execution results");

        // Log received data
        System.out.println("All variables received: " + (allVars != null ? allVars.size() : "null"));
        if (allVars != null) {
            for (VariableDTO var : allVars) {
                System.out.println("  All var: " + var.getName() + " = " + var.getValue() + " (type: " + var.getType() + ", isInput: " + var.isInput() + ")");
            }
        }

        System.out.println("Input variables received: " + (inputVars != null ? inputVars.size() : "null"));
        if (inputVars != null) {
            for (VariableDTO var : inputVars) {
                System.out.println("  Input var: " + var.getName() + " = " + var.getValue() + " (type: " + var.getType() + ")");
            }
        }

        System.out.println("Changed variables: " + (changedNames != null ? changedNames.toString() : "null"));

        // Left table: all/work/output variables
        var all = javafx.collections.FXCollections.observableArrayList(allVars);
        allVarsTable.setItems(all);
        System.out.println("Updated all variables table with " + all.size() + " items");

        // Right table: inputs
        var inputs = javafx.collections.FXCollections.observableArrayList(inputVars);
        inputVarsTable.setItems(inputs);
        System.out.println("Updated input variables table with " + inputs.size() + " items");

        // simple highlight for changed names
        allVarsTable.setRowFactory(tv -> new TableRow<VariableDTO>() {
            @Override protected void updateItem(VariableDTO item, boolean empty) {
                super.updateItem(item, empty);
                boolean changed = !empty && item != null && changedNames != null
                        && changedNames.contains(item.getName());
                setStyle(changed ? "-fx-background-color: lightgreen;" : "");
            }
        });
        inputVarsTable.setRowFactory(tv -> new TableRow<VariableDTO>() {
            @Override protected void updateItem(VariableDTO item, boolean empty) {
                super.updateItem(item, empty);
                boolean changed = !empty && item != null && changedNames != null
                        && changedNames.contains(item.getName());
                setStyle(changed ? "-fx-background-color: lightgreen;" : "");
            }
        });

        // column factories — run once if you haven't already:
        if (allVarsTable.getColumns().size() == 2) {
            @SuppressWarnings("unchecked")
            TableColumn<VariableDTO, String> nameCol =
                    (TableColumn<VariableDTO, String>) allVarsTable.getColumns().get(0);
            @SuppressWarnings("unchecked")
            TableColumn<VariableDTO, Number> valueCol =
                    (TableColumn<VariableDTO, Number>) allVarsTable.getColumns().get(1);
            nameCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
            valueCol.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getValue()));
        }
        if (inputVarsTable.getColumns().size() == 2) {
            @SuppressWarnings("unchecked")
            TableColumn<VariableDTO, String> nameCol =
                    (TableColumn<VariableDTO, String>) inputVarsTable.getColumns().get(0);
            @SuppressWarnings("unchecked")
            TableColumn<VariableDTO, Number> valueCol =
                    (TableColumn<VariableDTO, Number>) inputVarsTable.getColumns().get(1);
            nameCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
            
            // Use a simple integer wrapper that allows editing
            valueCol.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getValue()));

            // Make input variables table value column editable
            valueCol.setCellFactory(col -> new TableCell<VariableDTO, Number>() {
                private TextField textField;

                @Override
                protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            if (textField != null) {
                                textField.setText(item.toString());
                            }
                            setText(null);
                            setGraphic(textField);
                        } else {
                            setText(item.toString());
                            setGraphic(null);
                        }
                    }
                }

                @Override
                public void startEdit() {
                    if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                        return;
                    }
                    super.startEdit();

                    if (textField == null) {
                        createTextField();
                    }

                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                    textField.requestFocus();
                }

                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem() != null ? getItem().toString() : "");
                    setGraphic(null);
                }

                @Override
                public void commitEdit(Number newValue) {
                    super.commitEdit(newValue);
                    VariableDTO variable = getTableView().getItems().get(getIndex());
                    variable.setValue(newValue.intValue());
                    
                    System.out.println("Committed edit: " + variable.getName() + " = " + newValue.intValue());
                    
                    // IMPORTANT: Send the updated value to the server immediately
                    if (parent != null) {
                        System.out.println("Automatically sending input variable update to server: " + variable.getName() + " = " + newValue.intValue());
                        parent.updateInputValue(variable.getName(), newValue.intValue());
                    } else {
                        System.err.println("Cannot send input variable update - parent controller is null");
                    }

                    // Update the display immediately without full table refresh
                    setText(newValue.toString());
                    setGraphic(null);
                }

                private void createTextField() {
                    textField = new TextField(getItem() == null ? "" : getItem().toString());
                    textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                    
                    textField.setOnAction(e -> {
                        try {
                            int value = Integer.parseInt(textField.getText());
                            commitEdit(value);
                        } catch (NumberFormatException ex) {
                            System.err.println("Invalid number format: " + textField.getText());
                            cancelEdit();
                        }
                    });
                    
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused && isEditing()) {
                            try {
                                int value = Integer.parseInt(textField.getText());
                                commitEdit(value);
                            } catch (NumberFormatException ex) {
                                System.err.println("Invalid number format on focus lost: " + textField.getText());
                                cancelEdit();
                            }
                        }
                    });
                }
            });

            // Enable editing on the input variables table
            inputVarsTable.setEditable(true);
            valueCol.setEditable(true);
        }

        allVarsTable.refresh();
        inputVarsTable.refresh();
        System.out.println("Tables refreshed successfully");
        System.out.println("==================== VARIABLES UPDATE COMPLETE ====================");
    }


    public void setMainController(ExecutionDashboardController parent) {
        this.parent = parent;
    }



    // Must be public (dashboard calls it)
    public void setCyclesLabel(int cycles) {
        System.out.println("==================== CYCLES UPDATE ====================");
        System.out.println("setCyclesLabel called with cycles: " + cycles);

        if (cyclesLabel != null) {
            String oldText = cyclesLabel.getText();
            cyclesLabel.setText("Cycles: " + cycles);
            System.out.println("Cycles label updated from '" + oldText + "' to 'Cycles: " + cycles + "'");
        } else {
            System.err.println("Cycles label is null - cannot update cycles display");
        }

        System.out.println("==================== CYCLES UPDATE COMPLETE ====================");
    }

    // New signature used by dashboard
    public void updateDebugButtons(boolean debugging) {
        stepOverButton.setDisable(!debugging);
        continueButton.setDisable(!debugging);
        stopDebugButton.setDisable(!debugging);
        // start/execute/newRun can remain enabled as you prefer
    }

    public void executeButtonPressed(ActionEvent actionEvent) {
        if (parent == null) {
            System.err.println("Parent controller not set - cannot execute program");
            return;
        }

        System.out.println("==================== EXECUTE BUTTON PRESSED ====================");
        System.out.println("Execute button pressed - collecting input variables and executing program");

        // Collect and update input variable values before execution
        if (inputVarsTable != null && inputVarsTable.getItems() != null) {
            System.out.println("Input variables table found with " + inputVarsTable.getItems().size() + " variables");

            for (VariableDTO var : inputVarsTable.getItems()) {
                System.out.println("Processing input variable: " + var.getName() + " = " + var.getValue());

                // Send each input variable value to the server
                // The server needs to know the current values before execution
                if (parent != null) {
                    System.out.println("Sending to server: updateInputValue('" + var.getName() + "', " + var.getValue() + ")");
                    parent.updateInputValue(var.getName(), var.getValue());
                } else {
                    System.err.println("Parent controller is null - cannot send variable update for: " + var.getName());
                }
            }

            System.out.println("Completed sending " + inputVarsTable.getItems().size() + " input variable updates to server");
        } else {
            if (inputVarsTable == null) {
                System.out.println("Input variables table is null - no input variables to send");
            } else {
                System.out.println("Input variables table items is null - no input variables to send");
            }
        }

        // Log current state before execution
        System.out.println("Current cycles before execution: " + (cyclesLabel != null ? cyclesLabel.getText() : "unknown"));
        if (allVarsTable != null && allVarsTable.getItems() != null) {
            System.out.println("Current all variables count: " + allVarsTable.getItems().size());
            for (VariableDTO var : allVarsTable.getItems()) {
                System.out.println("  - " + var.getName() + " = " + var.getValue() + " (type: " + var.getType() + ")");
            }
        }

        // Execute the program using the parent controller's method
        // This will make an HTTP call to the server and update all UI components
        // The server will execute with the current degree and updated input variables
        System.out.println("Calling parent.executeProgram() to trigger server execution...");
        parent.executeProgram();

        System.out.println("Program execution request sent to server");
        System.out.println("==================== EXECUTE REQUEST COMPLETE ====================");
    }

    public void debugButtonPressed(ActionEvent actionEvent) {
        if (parent == null) {
            System.err.println("Parent controller not set - cannot start debugging");
            return;
        }

        System.out.println("Debug button pressed - starting debugging mode");
        parent.startDebugging();
    }

    public void newRunButtonPressed(ActionEvent actionEvent) {
        if (parent == null) {
            System.err.println("Parent controller not set - cannot create new run");
            return;
        }

        System.out.println("New run button pressed - creating fresh execution");
        parent.newRunButtonPressed();
    }

    public void stepOverPressed(ActionEvent actionEvent) {
        if (parent == null) {
            System.err.println("Parent controller not set - cannot step over");
            return;
        }

        System.out.println("Step over button pressed - stepping to next instruction");
        parent.stepOver();
    }

    public void stopDebugPressed(ActionEvent actionEvent) {
        if (parent == null) {
            System.err.println("Parent controller not set - cannot stop debugging");
            return;
        }

        System.out.println("Stop debug button pressed - stopping debugging mode");
        parent.stopDebugging();
    }

    public void continueButtonPressed(ActionEvent actionEvent) {
        if (parent == null) {
            System.err.println("Parent controller not set - cannot continue debugging");
            return;
        }

        System.out.println("Continue button pressed - continuing execution");
        parent.continueDebugging();
    }

    public void backToMainDashBoard(ActionEvent actionEvent) {
        try {
            // Load the main dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/mainDashboard/mainDashboard.fxml"));
            Parent mainDashboardRoot = loader.load();

            // Get the current stage from the button (using your proven pattern)
            Stage stage = (Stage) backToMainDashBoardButton.getScene().getWindow();

            // Apply the same transition pattern that works well
            if (stage != null && mainDashboardRoot != null) {
                if (stage.getScene() == null) {
                    stage.setScene(new Scene(mainDashboardRoot));
                } else {
                    stage.getScene().setRoot(mainDashboardRoot);
                }
                stage.setMaximized(true);
                stage.centerOnScreen();
            }

            // Set the title for the main dashboard
            stage.setTitle("S-Emulator - Main Dashboard");

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error gracefully - could show an alert dialog here if needed
        }
    }
}