package ui.statPanel;

import engine.Program;
import engine.Stats;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.base.BaseController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class statsPanelController {
    @FXML private TableView<ExecutionRecord> statsTable;
    @FXML private TableColumn<ExecutionRecord, Number> executionNumberColumn;
    @FXML private TableColumn<ExecutionRecord, Number> expansionLevelColumn;
    @FXML private TableColumn<ExecutionRecord, Number> cyclesColumn;
    @FXML private TableColumn<ExecutionRecord, String> outputColumn;
    @FXML private Button showStatusButton;
    @FXML private Button reRunButton;
    private BaseController mainController;
    private Stats lastStats;

    public void setMainController(BaseController baseController) {
        this.mainController = baseController;
    }

    @FXML
    private void initialize() {
        executionNumberColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getExecutionNumber()));
        expansionLevelColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getExpansionLevel()));
        cyclesColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getCycles()));
        outputColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getOutput()));
    }


    // Java
    public void showStatusButtonPressed(ActionEvent actionEvent) {
        ExecutionRecord selected = statsTable.getSelectionModel().getSelectedItem();
        if (selected == null || lastStats == null) return;

        Stats.Execution exec = lastStats.getExecutionHistory().stream()
                .filter(e -> e.getExecutionNumber() == selected.getExecutionNumber())
                .findFirst().orElse(null);

        if (exec == null) return;

        int expansionLevel = selected.getExpansionLevel();
        StringBuilder sb = new StringBuilder();
        sb.append("Variables at Expansion Level ").append(expansionLevel).append(":\n");

        try {
            // Find the correct engine for this execution
            Program programToExpand = null;
            if (mainController != null) {
                Object rtEngine = mainController.getheaderComponentController().getSelectedFunction();
                if (rtEngine != null) {
                    if (rtEngine.toString().equals(mainController.getEngine().getCurrentProgramName())) {
                        programToExpand = (Program) mainController.getEngine();
                    } else {
                        for (Program sub : mainController.getEngine().getSunFunctions()) {
                            if (rtEngine.toString().equals(sub.getUserString())) {
                                programToExpand = sub;
                                break;
                            }
                        }
                    }
                }
            }

            List<Command> displayedCommands = programToExpand.getCommandsAtDesiredLevel(expansionLevel);

            Set<Variable> displayedVars = new LinkedHashSet<>();
            Set<Variable> inputVars = new LinkedHashSet<>();
            for (Command cmd : displayedCommands) {
                Set<Variable> cmdVars = cmd.getAllVariables();
                if (cmdVars != null) {
                    for (Variable v : cmdVars) {
                        if (v instanceof WorkVariable || v instanceof OutputVariable) {
                            displayedVars.add(v);
                        }
                        if (v instanceof InputVariable) {
                            inputVars.add(v);
                        }
                    }
                }
            }

            // Show only input variables relevant to this expansion level
            sb.append("Input Variables at the start of the execution:");
            List<Variable> execInputVars = selected.getInputVars();
            if (execInputVars != null) {
                for (Variable var : execInputVars) {
                    if (var instanceof InputVariable) {
                        sb.append("\n[Input] ").append(var.getName()).append(" = ").append(var.getValue());
                    }
                }
            }

            this.mainController.sortAllVars(displayedVars);
            sb.append("\nWork and Output Variables after the execution:\n");
            for (var v : displayedVars) {
                if (v.getName().startsWith("z")) {
                    sb.append("[Work] ").append(v.getName()).append(" = ").append(v.getValue()).append("\n");
                } else if (v.getName().startsWith("y")) {
                    sb.append("[Output] ").append(v.getName()).append(" = ").append(v.getValue()).append("\n");
                }
            }

        } catch (Exception e) {
            sb.append("Error retrieving variables.");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Execution Status");
        alert.setHeaderText("Variables for Execution #" + exec.getExecutionNumber());
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    public void reRunButtonPressed(ActionEvent actionEvent) {
        // 1. Get the selected execution record from the stats table
        ui.statPanel.statsPanelController.ExecutionRecord execRecord = (ui.statPanel.statsPanelController.ExecutionRecord) statsTable.getSelectionModel().getSelectedItem();
        if (execRecord == null) return;

        // 2. Update the degree in the header controller (and update instructions table)
        String functionName = mainController.getheaderComponentController().getSelectedFunction().toString();
        int expansionLevel = execRecord.getExpansionLevel();
        mainController.setCurrentDegree(functionName, expansionLevel);

        // 3. Zero all variables (reset state)
        mainController.newRunButtonPressed();

        // 4. Set input variable values from the selected execution
        List<Variable> inputVars = execRecord.getInputVars(); // or exec.getInputVariables() if private
        if (inputVars != null) {
            // Find the current program and set input variable values
            Program program = (Program) mainController.getEngine();
            if(!functionName.equals(program.getCurrentProgramName())) {
                for(Program sub : program.getSunFunctions()) {
                    if(functionName.equals(sub.getUserString())) {
                        program = sub;
                        break;
                    }
                }
            }
            for (Variable var : inputVars) {
                if (var instanceof InputVariable) {
                    InputVariable inputVar = (InputVariable) var;
                    for (Variable engVar : program.getVariables()) {
                        if (engVar instanceof InputVariable && engVar.getName().equals(inputVar.getName())) {
                            ((InputVariable) engVar).setOriginalValue(inputVar.getValue());
                            engVar.setValue(inputVar.getValue());
                            break;
                        }
                    }
                }
            }

            List<Command> commandsAtLevel = program.getCommandsAtDesiredLevel(program.getCurrentDegree());

            Set<Variable> varsToShow = new LinkedHashSet<>();
            for (Command cmd : commandsAtLevel) {
                for(Variable v : cmd.getAssociatedVariables()) {
                    if(v instanceof InputVariable) {
                        varsToShow.add(v);
                    }
                }
            }

            // Update the input variable table in the execution panel
            mainController.getExecutuionPanelComponent().displayInputVars(varsToShow, null);
        }
    }

    public void updateStats(Stats executionHistory) {
        this.lastStats = executionHistory; // Store for later use
        ObservableList<ExecutionRecord> records = FXCollections.observableArrayList();
        for (Stats.Execution record : executionHistory.getExecutionHistory()) {
            int execNum = getExecutionNumber(record);
            int expansion = getExpansionLevel(record);
            int cycles = getCycleCount(record);
            List<Variable> inputVars = record.getInputVars();
            String output = getOutputValue(record);
            records.add(new ExecutionRecord(execNum, expansion, cycles, output, inputVars));
        }
        statsTable.setItems(records);
    }

    private int getExecutionNumber(Stats.Execution record) {
        return record.getExecutionNumber();
    }

    private int getExpansionLevel(Stats.Execution record) {
        try {
            var field = record.getClass().getDeclaredField("expansionLevel");
            field.setAccessible(true);
            return field.getInt(record);
        } catch (Exception e) {
            return 0;
        }
    }

    private int getCycleCount(Stats.Execution record) {
        try {
            var field = record.getClass().getDeclaredField("cycleCount");
            field.setAccessible(true);
            return field.getInt(record);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getOutputValue(Stats.Execution record) {
        try {
            var field = record.getClass().getDeclaredField("outputVariable");
            field.setAccessible(true);
            OutputVariable outVar = (OutputVariable) field.get(record);
            return outVar != null ? outVar.getName() + " = " + outVar.getValue() : "";
        } catch (Exception e) {
            return "";
        }
    }

    public class ExecutionRecord {
        private final int executionNumber;
        private final int expansionLevel;
        private final int cycles;
        private final String output;
        private final List<Variable> inputVars;

        public ExecutionRecord(int executionNumber, int expansionLevel, int cycles, String output, List<Variable> inputVars) {
            this.executionNumber = executionNumber;
            this.expansionLevel = expansionLevel;
            this.cycles = cycles;
            this.output = output;
            this.inputVars = inputVars;
        }

        public int getExecutionNumber() { return executionNumber; }
        public int getExpansionLevel() { return expansionLevel; }
        public int getCycles() { return cycles; }
        public String getOutput() { return output; }

        public List<Variable> getInputVars() {
            return inputVars;
        }
    }
}


