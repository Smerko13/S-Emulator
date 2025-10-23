// components/executionDashboard/executionPanel/ExecutionPanelController.java
package components.executionDashboard.executionPanel;

import api.dto.VariableDTO;
import api.dto.Architecture;
import components.executionDashboard.ExecutionDashboardController;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ExecutionPanelController implements Initializable {

    @FXML private Button backToMainDashBoardButton;
    @FXML private ComboBox<Architecture> architectureComboBox;
    @FXML private Button newRunButton, stepOverButton, continueButton, stopDebugButton, startDebugButton, programExecuteButton;
    @FXML private TableView<VariableDTO> inputVarsTable;
    @FXML private TableView<VariableDTO> allVarsTable;
    @FXML private Label cyclesLabel;

    private ExecutionDashboardController parent;
    private Parent mainDashboardRoot;  // Store reference to main dashboard to preserve state

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Initialize architecture ComboBox
        if (architectureComboBox != null) {

            // Populate ComboBox with all available architectures
            architectureComboBox.setItems(FXCollections.observableArrayList(Architecture.values()));

            // Set default selection to Generation I (cheapest)
            architectureComboBox.setValue(Architecture.GENERATION_I);

            // Add listener to log architecture changes
            architectureComboBox.setOnAction(e -> {
                Architecture selected = architectureComboBox.getValue();

            });

        }

    }

    /**
     * Gets the currently selected architecture from the ComboBox
     * @return Selected architecture, defaults to GENERATION_I if none selected
     */
    public Architecture getSelectedArchitecture() {
        if (architectureComboBox != null && architectureComboBox.getValue() != null) {
            Architecture selected = architectureComboBox.getValue();
            return selected;
        }

        return Architecture.GENERATION_I;
    }

    /**
     * Sets the selected architecture in the ComboBox
     * @param architecture The architecture to select
     */
    public void setSelectedArchitecture(Architecture architecture) {
        if (architectureComboBox != null && architecture != null) {
            architectureComboBox.setValue(architecture);
        }
    }

    public void setVariables(java.util.List<VariableDTO> allVars,
                             java.util.List<VariableDTO> inputVars,
                             java.util.Set<String> changedNames) {

        // Sort variables for each table
        // Left table: y and z* variables (work/output variables)
        // Right table: x* variables (input variables)
        // After execution: sort by value (non-zero first), then by numeric suffix
        boolean afterExecution = (changedNames != null);

        java.util.List<VariableDTO> sortedAllVars = sortVariablesForDisplay(allVars, afterExecution);
        java.util.List<VariableDTO> sortedInputVars = sortVariablesByValueAndIndex(inputVars, afterExecution);

        // Left table: y (first) and z1, z2, z3... (sorted by value if after execution)
        var all = javafx.collections.FXCollections.observableArrayList(sortedAllVars);
        allVarsTable.setItems(all);

        // Right table: x1, x2, x3... (sorted by value if after execution)
        var inputs = javafx.collections.FXCollections.observableArrayList(sortedInputVars);
        inputVarsTable.setItems(inputs);

        // Highlight non-zero values (end-of-run highlighting)
        // Only highlight if changedNames is provided (indicating execution completed)
        allVarsTable.setRowFactory(tv -> new TableRow<VariableDTO>() {
            @Override protected void updateItem(VariableDTO item, boolean empty) {
                super.updateItem(item, empty);
                // Highlight if value ≠ 0 and changedNames is not null (execution completed)
                boolean shouldHighlight = !empty && item != null && changedNames != null
                        && item.getValue() != 0;
                setStyle(shouldHighlight ? "-fx-background-color: lightgreen;" : "");
            }
        });
        inputVarsTable.setRowFactory(tv -> new TableRow<VariableDTO>() {
            @Override protected void updateItem(VariableDTO item, boolean empty) {
                super.updateItem(item, empty);
                // Highlight if value ≠ 0 and changedNames is not null (execution completed)
                boolean shouldHighlight = !empty && item != null && changedNames != null
                        && item.getValue() != 0;
                setStyle(shouldHighlight ? "-fx-background-color: lightgreen;" : "");
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
                    

                    // IMPORTANT: Send the updated value to the server immediately
                    if (parent != null) {
                        parent.updateInputValue(variable.getName(), newValue.intValue());
                    } else {
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
                            cancelEdit();
                        }
                    });
                    
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused && isEditing()) {
                            try {
                                int value = Integer.parseInt(textField.getText());
                                commitEdit(value);
                            } catch (NumberFormatException ex) {
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
    }

    /**
     * Sort variables for display: y first, then z1..zm
     * After execution: sort by value (non-zero first), then by numeric index
     */
    private java.util.List<VariableDTO> sortVariablesForDisplay(java.util.List<VariableDTO> vars, boolean afterExecution) {
        java.util.List<VariableDTO> result = new java.util.ArrayList<>();
        java.util.List<VariableDTO> yVars = new java.util.ArrayList<>();
        java.util.List<VariableDTO> zVars = new java.util.ArrayList<>();

        // Separate variables by type
        for (VariableDTO var : vars) {
            String name = var.getName();
            if (name.equals("y")) {
                yVars.add(var);
            } else if (name.startsWith("z")) {
                zVars.add(var);
            }
        }

        if (afterExecution) {
            // Sort by value first (non-zero before zero), then by numeric suffix
            zVars.sort((v1, v2) -> {
                // Non-zero values come first
                boolean v1NonZero = v1.getValue() != 0;
                boolean v2NonZero = v2.getValue() != 0;
                if (v1NonZero != v2NonZero) {
                    return v1NonZero ? -1 : 1;
                }
                // Within same value group, sort by numeric suffix
                return compareVariableNames(v1, v2);
            });
        } else {
            // Before execution: just sort by numeric suffix
            zVars.sort(this::compareVariableNames);
        }

        // Combine: y first, then z*
        result.addAll(yVars);
        result.addAll(zVars);

        return result;
    }

    /**
     * Sort input variables by value (non-zero first) after execution, then by numeric suffix
     */
    private java.util.List<VariableDTO> sortVariablesByValueAndIndex(java.util.List<VariableDTO> vars, boolean afterExecution) {
        java.util.List<VariableDTO> sorted = new java.util.ArrayList<>(vars);

        if (afterExecution) {
            // Sort by value first (non-zero before zero), then by numeric suffix
            sorted.sort((v1, v2) -> {
                // Non-zero values come first
                boolean v1NonZero = v1.getValue() != 0;
                boolean v2NonZero = v2.getValue() != 0;
                if (v1NonZero != v2NonZero) {
                    return v1NonZero ? -1 : 1;
                }
                // Within same value group, sort by numeric suffix
                return compareVariableNames(v1, v2);
            });
        } else {
            // Before execution: just sort by numeric suffix
            sorted.sort(this::compareVariableNames);
        }

        return sorted;
    }

    /**
     * Compare two variable names by their numeric suffix
     * Handles cases like x2 < x10 (numeric comparison, not lexicographic)
     */
    private int compareVariableNames(VariableDTO v1, VariableDTO v2) {
        String name1 = v1.getName();
        String name2 = v2.getName();

        // Extract numeric suffix
        Integer num1 = extractNumericSuffix(name1);
        Integer num2 = extractNumericSuffix(name2);

        // If both have numeric suffixes, compare numerically
        if (num1 != null && num2 != null) {
            return num1.compareTo(num2);
        }

        // Fallback to lexicographic comparison
        return name1.compareTo(name2);
    }

    /**
     * Extract numeric suffix from variable name (e.g., "x12" -> 12, "z5" -> 5)
     */
    private Integer extractNumericSuffix(String name) {
        if (name == null || name.length() <= 1) {
            return null;
        }

        // Find where digits start
        int digitStart = -1;
        for (int i = 1; i < name.length(); i++) {
            if (Character.isDigit(name.charAt(i))) {
                digitStart = i;
                break;
            }
        }

        if (digitStart == -1) {
            return null;
        }

        try {
            return Integer.parseInt(name.substring(digitStart));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setMainController(ExecutionDashboardController parent) {
        this.parent = parent;
    }

    /**
     * Set the main dashboard root to return to when back button is pressed.
     * This preserves the state of the main dashboard (programs list, user info, etc.)
     */
    public void setMainDashboardRoot(Parent mainDashboardRoot) {
        this.mainDashboardRoot = mainDashboardRoot;
    }



    // Must be public (dashboard calls it)
    public void setCyclesLabel(int cycles) {

        if (cyclesLabel != null) {
            String oldText = cyclesLabel.getText();
            cyclesLabel.setText("Cycles: " + cycles);
        }

    }

    // New signature used by dashboard
    public void updateDebugButtons(boolean debugging) {
        stepOverButton.setDisable(!debugging);
        continueButton.setDisable(!debugging);
        stopDebugButton.setDisable(!debugging);
        // start/execute/newRun can remain enabled as you prefer
    }

    /**
     * Update input display values (used during Re-Run to pre-fill inputs)
     */
    public void updateInputDisplayValues(java.util.List<VariableDTO> preFilledInputs) {

        if (inputVarsTable == null || inputVarsTable.getItems() == null) {
            return;
        }

        // Update the values in the input table
        for (VariableDTO preFilledInput : preFilledInputs) {
            for (VariableDTO tableInput : inputVarsTable.getItems()) {
                if (tableInput.getName().equals(preFilledInput.getName())) {
                    tableInput.setValue(preFilledInput.getValue());
                    break;
                }
            }
        }

        // Refresh the table to show updated values
        inputVarsTable.refresh();
    }

    public void executeButtonPressed(ActionEvent actionEvent) {
        if (parent == null) {
            return;
        }


        // Get the selected architecture and validate credits
        Architecture selectedArchitecture = getSelectedArchitecture();

        // Validate architecture compatibility with instruction table
        Architecture requiredArchitecture = validateArchitectureWithInstructionTable(selectedArchitecture);
        if (requiredArchitecture != null) {
            // Architecture is insufficient - show error and block execution
            showArchitectureInsufficientDialog(selectedArchitecture, requiredArchitecture);
            return;
        }

        // Check if user has sufficient credits
        components.shared.UserSession userSession = components.shared.UserSession.getInstance();
        int availableCredits = userSession.getCredits();
        int architectureCost = selectedArchitecture.getCost();


        // TODO: Get average program cost from server/context if available
        int estimatedCycleCost = 50; // Conservative estimate for cycles
        int totalEstimatedCost = architectureCost + estimatedCycleCost;

        if (availableCredits < totalEstimatedCost) {
            // Insufficient credits - show warning dialog
            showInsufficientCreditsDialog(availableCredits, totalEstimatedCost, architectureCost);
            return;
        }

        // Collect and update input variable values before execution
        if (inputVarsTable != null && inputVarsTable.getItems() != null) {

            for (VariableDTO var : inputVarsTable.getItems()) {

                // Send each input variable value to the server
                // The server needs to know the current values before execution
                if (parent != null) {
                    parent.updateInputValue(var.getName(), var.getValue());
                }
            }

        }



        // Execute the program using the parent controller's method with selected architecture
        // This will make an HTTP call to the server and update all UI components
        // The server will execute with the current degree, updated input variables, and selected architecture
        parent.executeProgram(selectedArchitecture);
}

    /**
     * Validate architecture compatibility with instruction table controller
     */
    private Architecture validateArchitectureWithInstructionTable(Architecture selectedArchitecture) {
        // Get instruction table controller from parent
        if (parent != null) {
            return parent.validateArchitectureCompatibility(selectedArchitecture);
        }
        return null;
    }

    /**
     * Show dialog when selected architecture is insufficient
     */
    private void showArchitectureInsufficientDialog(Architecture selected, Architecture required) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Insufficient Architecture");
        alert.setHeaderText("Selected architecture cannot execute this program");

        StringBuilder content = new StringBuilder();
        content.append("The program contains instructions that require ").append(required.getDisplayName()).append(".\n\n");
        content.append("Selected: ").append(selected.getDisplayName()).append(" (").append(selected.getCost()).append(" credits)\n");
        content.append("Required: ").append(required.getDisplayName()).append(" (").append(required.getCost()).append(" credits)\n\n");
        content.append("Incompatible instructions have been highlighted in red.\n");
        content.append("Please select ").append(required.getDisplayName()).append(" or higher to execute.");

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    /**
     * Show dialog when user has insufficient credits
     */
    private void showInsufficientCreditsDialog(int available, int estimated, int architectureCost) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Insufficient Credits");
        alert.setHeaderText("Not enough credits to execute program");

        StringBuilder content = new StringBuilder();
        content.append("You do not have enough credits to execute this program.\n\n");
        content.append("Available credits: ").append(available).append("\n");
        content.append("Architecture base cost: ").append(architectureCost).append("\n");
        content.append("Estimated total cost: ").append(estimated).append("\n\n");
        content.append("Please add more credits before executing.");

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    public void debugButtonPressed(ActionEvent actionEvent) {
        if (parent == null) {
            return;
        }


        // Get the selected architecture and validate credits
        Architecture selectedArchitecture = getSelectedArchitecture();

        // Validate architecture compatibility with instruction table
        Architecture requiredArchitecture = validateArchitectureWithInstructionTable(selectedArchitecture);
        if (requiredArchitecture != null) {
            // Architecture is insufficient - show error and block execution
            showArchitectureInsufficientDialog(selectedArchitecture, requiredArchitecture);
            return;
        }

        // Check if user has sufficient credits
        components.shared.UserSession userSession = components.shared.UserSession.getInstance();
        int availableCredits = userSession.getCredits();
        int architectureCost = selectedArchitecture.getCost();


        // For debug mode, we only check if user has enough for architecture cost
        // (stepping costs are charged per step)
        if (availableCredits < architectureCost) {
            // Insufficient credits - show warning dialog
            showInsufficientCreditsDialog(availableCredits, architectureCost, architectureCost);
            return;
        }

        // Collect and update input variable values before debugging
        if (inputVarsTable != null && inputVarsTable.getItems() != null) {

            for (VariableDTO var : inputVarsTable.getItems()) {

                // Send each input variable value to the server
                if (parent != null) {
                    parent.updateInputValue(var.getName(), var.getValue());
                }
            }

        }

        // Start debugging with the selected architecture
        parent.startDebugging(selectedArchitecture);

   }

    public void newRunButtonPressed(ActionEvent actionEvent) {
        if (parent == null) {
            return;
        }

        parent.newRunButtonPressed();
    }

    public void stepOverPressed(ActionEvent actionEvent) {
        if (parent == null) {
            return;
        }

        parent.stepOver();
    }

    public void stopDebugPressed(ActionEvent actionEvent) {
        if (parent == null) {
            return;
        }

        parent.stopDebugging();
    }

    public void continueButtonPressed(ActionEvent actionEvent) {
        if (parent == null) {
            return;
        }

        parent.continueDebugging();
    }

    public void backToMainDashBoard(ActionEvent actionEvent) {
        try {
            // Get the current stage from the button
            Stage stage = (Stage) backToMainDashBoardButton.getScene().getWindow();

            if (stage == null) {
                return;
            }

            // If we have a reference to the main dashboard root, use it (preserves state)
            if (mainDashboardRoot != null) {

                if (stage.getScene() == null) {
                    stage.setScene(new Scene(mainDashboardRoot));
                } else {
                    stage.getScene().setRoot(mainDashboardRoot);
                }
                stage.setMaximized(true);
                stage.centerOnScreen();
                stage.setTitle("S-Emulator - Main Dashboard");
            } else {
                // Fallback: Load fresh instance if no reference was provided
                // This shouldn't happen in normal flow, but provides backward compatibility

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/mainDashboard/mainDashboard.fxml"));
                Parent mainDashboardRoot = loader.load();

                if (stage.getScene() == null) {
                    stage.setScene(new Scene(mainDashboardRoot));
                } else {
                    stage.getScene().setRoot(mainDashboardRoot);
                }
                stage.setMaximized(true);
                stage.centerOnScreen();
                stage.setTitle("S-Emulator - Main Dashboard");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
