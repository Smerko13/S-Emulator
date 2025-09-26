package ui.base;

import engine.Engine;
import engine.S_Emulator;
import engine.Stats;
import engine.arguments.Variable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.executionPanelController.ExecutionPanelController;
import ui.header.HeaderController;
import ui.historyPanel.HistoryPanelController;
import ui.instructionTable.InstructionTableController;
import ui.statPanel.StatPanelController;

import java.io.File;
import java.util.*;

import static java.lang.Thread.sleep;

public class BaseController {
    @FXML private HeaderController headerComponentController;
    @FXML private InstructionTableController instructionTableComponentController;
    @FXML private ExecutionPanelController executionPanelComponentController;
    @FXML private HistoryPanelController historyPanelComponentController;
    @FXML private StatPanelController statPanelComponentController;
    S_Emulator s_emulator;
    List<S_Emulator> programHistory;
    private boolean isFileLoaded = false;
    private boolean isDebuggingEnabled = false;
    private Engine selectedEngine = null;

    public boolean isDebuggingEnabled() {
        return isDebuggingEnabled;
    }
    public boolean isFileLoaded() {
        return isFileLoaded;
    }

    @FXML
    public void initialize() {
        if(headerComponentController != null
                && instructionTableComponentController != null
                && executionPanelComponentController != null
                && historyPanelComponentController != null
            && statPanelComponentController != null) {
            executionPanelComponentController.setMainController(this);
            headerComponentController.setMainController(this);
            instructionTableComponentController.setMainController(this);
            historyPanelComponentController.setMainController(this);
            statPanelComponentController.setMainController(this);
        }
        programHistory = new ArrayList<>();
    }


    public void loadFile(File selectedFile) throws InterruptedException {
        instructionTableComponentController.clearInstructions();
        executionPanelComponentController.clearAllVars();
        s_emulator = new Engine(true);
        programHistory.add(s_emulator);
        showLoadingProgress(() -> Platform.runLater(() -> {
            try {
                boolean fileLoadedSuccessfully = s_emulator.readProgramFromXml(selectedFile.toString());
                if (fileLoadedSuccessfully) {
                    isFileLoaded = true;
                    isDebuggingEnabled = false;
                    List<Command> displayedCommands = s_emulator.getCommands();
                    instructionTableComponentController.displayInstructions(displayedCommands);

                    Set<Variable> displayedVars = new LinkedHashSet<>();
                    Set<Variable> inputVars = new LinkedHashSet<>();
                    for (Command cmd : displayedCommands) {
                        Set<Variable> cmdVars = cmd.getAllVariables();
                        if (cmdVars != null) {
                            for (Variable v : cmdVars) {
                                if (v instanceof WorkVariable || v instanceof OutputVariable) {
                                    displayedVars.add(v);
                                }
                                if (v instanceof engine.arguments.types.InputVariable) {
                                    inputVars.add(v);
                                }
                            }
                        }
                    }
                    s_emulator.getVariables().forEach(v -> {
                        if (v instanceof OutputVariable) {
                            displayedVars.add(v);
                        }
                    });

                    executionPanelComponentController.displayAllVars(displayedVars);
                    executionPanelComponentController.displayInputVars(inputVars, null);
                    this.executionPanelComponentController.enableAllButtons();
                    List<String> functionNames = new ArrayList<>();
                    functionNames.add(s_emulator.getCurrentProgramName());
                    for (Engine sub : s_emulator.getSunFunctions()) {
                        functionNames.add(sub.getUserString());
                    }
                    // Call a method in HeaderController to update the selector
                    headerComponentController.updateFunctionSelector(functionNames);
                }
            } catch (IllegalArgumentException ex) {
                showErrorDialog(ex.getMessage());
            }
        }));
    }

    // Java
    public String getCurrentDegree() {
        Engine engine = selectedEngine != null ? selectedEngine : (Engine) s_emulator;
        return String.valueOf(engine.getCurrentDegree());
    }

    public String getMaxDegree() {
        Engine engine = selectedEngine != null ? selectedEngine : (Engine) s_emulator;
        return String.valueOf(engine.getMaxExpansionDepth());
    }

    public void setMonitors() {
        Set<String> labels = getSortedLabels();

        Set<Variable> variables = new TreeSet<>(
                Comparator.comparingInt((Variable v) -> v.getName().length())
                        .thenComparing(Variable::getName)
        );
        for(Command cmd : s_emulator.getCommandsAtDesiredLevel(s_emulator.getCurrentDegree())) {
            Set<Variable> cmdVars = cmd.getAllVariables();
            if(cmdVars != null) {
                variables.addAll(cmdVars);
            }
        }

        headerComponentController.setHeaderMonitors(labels, variables);
    }

    private Set<String> getSortedLabels() {
        Set<String> labels = new TreeSet<>((a, b) -> {
            String s1 = a.trim(), s2 = b.trim();
            int i = 0, j = 0, n1 = s1.length(), n2 = s2.length();

            while (i < n1 && j < n2) {
                char c1 = s1.charAt(i), c2 = s2.charAt(j);
                boolean d1 = Character.isDigit(c1), d2 = Character.isDigit(c2);

                if (d1 && d2) {
                    // consume leading zeros
                    int z1 = i; while (z1 < n1 && s1.charAt(z1) == '0') z1++;
                    int z2 = j; while (z2 < n2 && s2.charAt(z2) == '0') z2++;

                    // consume digit runs
                    int k1 = z1; while (k1 < n1 && Character.isDigit(s1.charAt(k1))) k1++;
                    int k2 = z2; while (k2 < n2 && Character.isDigit(s2.charAt(k2))) k2++;

                    int len1 = k1 - z1, len2 = k2 - z2;
                    if (len1 != len2) return Integer.compare(len1, len2);   // shorter number first

                    int cmp = s1.substring(z1, k1).compareTo(s2.substring(z2, k2));
                    if (cmp != 0) return cmp;

                    // same numeric value: fewer leading zeros first
                    int zerosCmp = Integer.compare(z1 - i, z2 - j);
                    if (zerosCmp != 0) return zerosCmp;

                    i = k1; j = k2;
                } else {
                    if (c1 != c2) return Character.compare(c1, c2);
                    i++; j++;
                }
            }
            if (i != n1 || j != n2) return Integer.compare(n1 - i, n2 - j);

            return s1.compareTo(s2);
        });

        labels.addAll(s_emulator.getLabels(s_emulator.getCurrentDegree()));
        return labels;
    }

    public void onHighlightSelectionChanged(Object selected) {
        instructionTableComponentController.setHighlight(selected);
        if (selected instanceof engine.commands.Command) {
            historyPanelComponentController.displayParentChain((engine.commands.Command) selected);
        } else {
            historyPanelComponentController.displayParentChain(null);
        }
    }

    public void executeProgram() {
        Map<String, Integer> prevValues = new HashMap<>();
        Engine engineToRun = selectedEngine != null ? selectedEngine : (Engine) s_emulator;
        for (Variable v : engineToRun.getVariables()) {
            prevValues.put(v.getName(), v.getValue());
        }

        // 2. Execute
        engineToRun.executeProgram(engineToRun.getCurrentDegree());

        // 3. Find changed variables
        Set<String> changedVars = new HashSet<>();
        for (Variable v : engineToRun.getVariables()) {
            Integer prev = prevValues.get(v.getName());
            if (prev != null && prev != v.getValue()) {
                changedVars.add(v.getName());
            }
        }

        int currExpansionLvl = engineToRun.getCurrentDegree();
        List<Command> displayedCommands = engineToRun.getCommandsAtDesiredLevel(currExpansionLvl);
        instructionTableComponentController.displayInstructions(displayedCommands);

        Set<Variable> displayedVars = new LinkedHashSet<>();
        Set<Variable> inputVars = new LinkedHashSet<>();
        for (Command cmd : displayedCommands) {
            Set<Variable> cmdVars = cmd.getAllVariables();
            if (cmdVars != null) {
                for (Variable v : cmdVars) {
                    if (v instanceof WorkVariable || v instanceof OutputVariable) {
                        displayedVars.add(v);
                    }
                    if (v instanceof engine.arguments.types.InputVariable) {
                        inputVars.add(v);
                    }
                }
            }
        }
        engineToRun.getVariables().forEach(v -> {
            if (v instanceof OutputVariable) {
                displayedVars.add(v);
            }
        });
        executionPanelComponentController.displayAllVars(displayedVars, changedVars);
        executionPanelComponentController.displayInputVars(inputVars, changedVars);
        executionPanelComponentController.setCyclesLabel(engineToRun.getCycleSum());
        statPanelComponentController.refreshExecutionNumbers(engineToRun.getExecutionHistory());
    }

    public Stats getStats() {
        return s_emulator.getExecutionHistory();
    }

    private void showLoadingProgress(Runnable onLoaded) {
        Stage progressStage = new Stage();
        progressStage.initModality(Modality.APPLICATION_MODAL);
        progressStage.setTitle("Loading...");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);

        VBox vbox = new VBox(progressBar);
        vbox.setSpacing(10);
        vbox.setStyle("-fx-padding: 20;");
        progressStage.setScene(new Scene(vbox));
        progressStage.setResizable(false);

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simulate loading steps
                for (int i = 1; i <= 3; i++) {
                    updateProgress(i, 3);
                    sleep(700); // Simulate work
                }
                return null;
            }

            @Override
            protected void succeeded() {
                progressStage.close();
                if (onLoaded != null) onLoaded.run();
            }
        };

        progressBar.progressProperty().bind(loadTask.progressProperty());
        progressStage.show();

        Thread t = new Thread(loadTask);
        t.setDaemon(true);
        t.start();
    }

    public void startDebugging() {
        if (!isFileLoaded) return;
        isDebuggingEnabled = true;
        Engine engineToDebug = selectedEngine != null ? selectedEngine : (Engine) s_emulator;
        engineToDebug.prepareForDebugging();
        Command currentDebugCommand = engineToDebug.getCurrentDebugCommand();
        instructionTableComponentController.setDebugHighlight(currentDebugCommand);
        executionPanelComponentController.updateDebugButtons();
    }

    public void stepOver() {
        s_emulator.stepOver();
        Command currentDebugCommand = s_emulator.getCurrentDebugCommand(); // You may need to add this getter
        instructionTableComponentController.setDebugHighlight(currentDebugCommand);
        Set<Variable> allVars = s_emulator.getVariables();
        Set<Variable> displayedVars = new LinkedHashSet<>();
        Set<Variable> inputVars = new LinkedHashSet<>();
        for (Variable v : allVars) {
            if (v instanceof WorkVariable || v instanceof OutputVariable) {
                displayedVars.add(v);
            }
            if (v instanceof engine.arguments.types.InputVariable) {
                inputVars.add(v);
            }
        }
        executionPanelComponentController.displayVarsForCurrentInstructions(
                s_emulator.getVariables(),
                s_emulator.getCommandsAtDesiredLevel(s_emulator.getCurrentDegree())
        );
        executionPanelComponentController.displayInputVars(inputVars, null);
        executionPanelComponentController.setCyclesLabel(s_emulator.getCycleSum());
        statPanelComponentController.refreshExecutionNumbers(s_emulator.getExecutionHistory());
    }

    public void stopDebugging() {
        // Reset engine state to initial values
        if (s_emulator instanceof Engine engine) {
            engine.reset();
        }
        // Refresh UI
        instructionTableComponentController.setDebugHighlight(null);
        executionPanelComponentController.updateDebugButtons();
        isDebuggingEnabled = false;

        // Update variable tables and stats
        // Update variable tables and stats
        Set<Variable> allVars = s_emulator.getVariables();
        Set<Variable> inputVars = new LinkedHashSet<>();
        for (Variable v : allVars) {
            if (v instanceof engine.arguments.types.InputVariable) {
                inputVars.add(v);
            }
        }
        executionPanelComponentController.displayVarsForCurrentInstructions(
                allVars,
                s_emulator.getCommandsAtDesiredLevel(s_emulator.getCurrentDegree())
        );
        executionPanelComponentController.displayInputVars(inputVars, null);
        executionPanelComponentController.setCyclesLabel(0);
        statPanelComponentController.refreshExecutionNumbers(s_emulator.getExecutionHistory());
    }

    public void continueDebugging() {
        while (isDebuggingEnabled && s_emulator.getCurrentDebugCommand() != null) {
            s_emulator.stepOver();
        }
        // Clear highlight and update UI
        instructionTableComponentController.setDebugHighlight(null);
        executionPanelComponentController.updateDebugButtons();
        // Update variable tables
        executionPanelComponentController.displayVarsForCurrentInstructions(
                s_emulator.getVariables(),
                s_emulator.getCommandsAtDesiredLevel(s_emulator.getCurrentDegree())
        );
        executionPanelComponentController.displayInputVars(s_emulator.getVariables(),null);
        executionPanelComponentController.setCyclesLabel(s_emulator.getCycleSum());
        statPanelComponentController.refreshExecutionNumbers(s_emulator.getExecutionHistory());
        // Disable debugging
        isDebuggingEnabled = false;
    }

    public void onFunctionSelectionChanged(String selectedName) {
        Engine engineToShow = null;
        if (selectedName.equals(s_emulator.getCurrentProgramName())) {
            engineToShow = (Engine) s_emulator;
        } else {
            for (Engine sub : ((Engine) s_emulator).getSunFunctions()) {
                if (selectedName.equals(sub.getUserString())) {
                    engineToShow = sub;
                    engineToShow.expandCommands();
                    break;
                }
            }
        }
        if (engineToShow != null) {
            selectedEngine = engineToShow; // Track the selected engine
            List<Command> displayedCommands = selectedEngine.getCommandsAtDesiredLevel(selectedEngine.getCurrentDegree());
            instructionTableComponentController.displayInstructions(displayedCommands);

            Set<Variable> displayedVars = new LinkedHashSet<>();
            Set<Variable> inputVars = new LinkedHashSet<>();
            for (Command cmd : displayedCommands) {
                Set<Variable> cmdVars = cmd.getAllVariables();
                if (cmdVars != null) {
                    for (Variable v : cmdVars) {
                        if (v instanceof WorkVariable || v instanceof OutputVariable) {
                            displayedVars.add(v);
                        }
                        if (v instanceof engine.arguments.types.InputVariable) {
                            inputVars.add(v);
                        }
                    }
                }
            }
            selectedEngine.getVariables().forEach(v -> {
                if (v instanceof OutputVariable) {
                    displayedVars.add(v);
                }
            });

            executionPanelComponentController.displayAllVars(displayedVars);
            executionPanelComponentController.displayInputVars(inputVars, null);
        }
    }

    public void expandProgram(String functionName) {
        Engine engineToExpand;
        if (functionName.equals(s_emulator.getCurrentProgramName())) {
            engineToExpand = (Engine) s_emulator;
        } else {
            engineToExpand = null;
            for (Engine sub : ((Engine) s_emulator).getSunFunctions()) {
                if (functionName.equals(sub.getUserString())) {
                    engineToExpand = sub;
                    break;
                }
            }
        }
        if (engineToExpand != null) {
            engineToExpand.increaseDegree();
            int currExpansionLvl = engineToExpand.getCurrentDegree();
            List<Command> displayedCommands = engineToExpand.getCommandsAtDesiredLevel(currExpansionLvl);

            Set<Variable> displayedVars = new LinkedHashSet<>();
            Set<Variable> inputVars = new LinkedHashSet<>();
            for (Command cmd : displayedCommands) {
                Set<Variable> cmdVars = cmd.getAllVariables();
                if (cmdVars != null) {
                    for (Variable v : cmdVars) {
                        if (v instanceof WorkVariable || v instanceof OutputVariable) {
                            displayedVars.add(v);
                        }
                        if (v instanceof engine.arguments.types.InputVariable) {
                            inputVars.add(v);
                        }
                    }
                }
            }
            engineToExpand.getVariables().forEach(v -> {
                if (v instanceof OutputVariable) {
                    displayedVars.add(v);
                }
            });
            instructionTableComponentController.displayInstructions(displayedCommands);
            executionPanelComponentController.displayAllVars(displayedVars);
            executionPanelComponentController.displayInputVars(inputVars, null);
        }
    }

    public void collapseProgram(String functionName) {
        Engine engineToCollapse;
        if (functionName.equals(s_emulator.getCurrentProgramName())) {
            engineToCollapse = (Engine) s_emulator;
        } else {
            engineToCollapse = null;
            for (Engine sub : ((Engine) s_emulator).getSunFunctions()) {
                if (functionName.equals(sub.getUserString())) {
                    engineToCollapse = sub;
                    break;
                }
            }
        }
        if (engineToCollapse != null) {
            engineToCollapse.decreaseDegree();
            int currExpansionLvl = engineToCollapse.getCurrentDegree();
            List<Command> displayedCommands = engineToCollapse.getCommandsAtDesiredLevel(currExpansionLvl);

            Set<Variable> displayedVars = new LinkedHashSet<>();
            Set<Variable> inputVars = new LinkedHashSet<>();
            for (Command cmd : displayedCommands) {
                Set<Variable> cmdVars = cmd.getAllVariables();
                if (cmdVars != null) {
                    for (Variable v : cmdVars) {
                        if (v instanceof WorkVariable || v instanceof OutputVariable) {
                            displayedVars.add(v);
                        }
                        if (v instanceof engine.arguments.types.InputVariable) {
                            inputVars.add(v);
                        }
                    }
                }
            }
            engineToCollapse.getVariables().forEach(v -> {
                if (v instanceof OutputVariable) {
                    displayedVars.add(v);
                }
            });
            instructionTableComponentController.displayInstructions(displayedCommands);
            executionPanelComponentController.displayAllVars(displayedVars);
            executionPanelComponentController.displayInputVars(inputVars, null);
        }
    }

    private void showErrorDialog(String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Program Load Error");
            alert.setHeaderText("The program is not valid.");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
