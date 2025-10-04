package ui.base;

import engine.Program;
import engine.S_Emulator;
import engine.Stats;
import engine.arguments.Variable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.synthetic.types.GotoLabel;
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
import ui.statPanel.statsPanelController;

import java.io.File;
import java.util.*;

import static java.lang.Thread.sleep;

public class BaseController {
    @FXML private HeaderController headerComponentController;
    @FXML private InstructionTableController instructionTableComponentController;
    @FXML private ExecutionPanelController executionPanelComponentController;
    @FXML private HistoryPanelController historyPanelComponentController;
    @FXML private statsPanelController statsComponentController;
    S_Emulator s_emulator;
    List<S_Emulator> programHistory;
    private boolean isFileLoaded = false;
    private boolean isDebuggingEnabled = false;
    private Program selectedProgram = null;

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
        && statsComponentController != null) {
            executionPanelComponentController.setMainController(this);
            headerComponentController.setMainController(this);
            instructionTableComponentController.setMainController(this);
            historyPanelComponentController.setMainController(this);
            statsComponentController.setMainController(this);
        }
        programHistory = new ArrayList<>();
    }


    public void loadFile(File selectedFile) throws InterruptedException {
        instructionTableComponentController.clearInstructions();
        executionPanelComponentController.clearAllVars();
        s_emulator = new Program(true);
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
                        if(cmd instanceof GotoLabel) { continue;}
                        Variable[] cmdVars = cmd.getAssociatedVariables();
                        if (cmdVars != null) {
                            for (Variable v : cmdVars) {
                                if(cmd instanceof GotoLabel) { continue;}
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

                    sortAllVars(displayedVars);
                    executionPanelComponentController.displayAllVars(displayedVars);
                    executionPanelComponentController.displayInputVars(inputVars, null);
                    this.executionPanelComponentController.enableAllButtons();
                    List<String> functionNames = new ArrayList<>();
                    functionNames.add(s_emulator.getCurrentProgramName());
                    for (Program sub : s_emulator.getSunFunctions()) {
                        functionNames.add(sub.getCurrentProgramName());
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
        Program program = selectedProgram != null ? selectedProgram : (Program) s_emulator;
        return String.valueOf(program.getCurrentDegree());
    }

    public String getMaxDegree() {
        Program program = selectedProgram != null ? selectedProgram : (Program) s_emulator;
        return String.valueOf(program.getMaxExpansionDepth());
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
        Program programToRun = selectedProgram != null ? selectedProgram : (Program) s_emulator;
        for (Variable v : programToRun.getVariables()) {
            prevValues.put(v.getName(), v.getValue());
        }

        // 2. Execute
        programToRun.executeProgram(programToRun.getCurrentDegree(),true);

        // 3. Find changed variables
        Set<String> changedVars = new HashSet<>();
        for (Variable v : programToRun.getVariables()) {
            Integer prev = prevValues.get(v.getName());
            if (prev != null && prev != v.getValue()) {
                changedVars.add(v.getName());
            }
        }

        int currExpansionLvl = programToRun.getCurrentDegree();
        List<Command> displayedCommands = programToRun.getCommandsAtDesiredLevel(currExpansionLvl);
        instructionTableComponentController.displayInstructions(displayedCommands);

        Set<Variable> displayedVars = new LinkedHashSet<>();
        Set<Variable> inputVars = new LinkedHashSet<>();
        for (Command cmd : displayedCommands) {
            Variable[] cmdVars = cmd.getAssociatedVariables();
            if (cmdVars != null) {
                if(cmd instanceof GotoLabel) { continue;}
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
        programToRun.getVariables().forEach(v -> {
            if (v instanceof OutputVariable) {
                displayedVars.add(v);
            }
        });

        sortAllVars(displayedVars);

        executionPanelComponentController.displayAllVars(displayedVars, changedVars);
        executionPanelComponentController.displayInputVars(inputVars, changedVars);
        executionPanelComponentController.setCyclesLabel(programToRun.getCycleSum());
        statsComponentController.updateStats(programToRun.getExecutionHistory());
    }

    public void sortAllVars(Set<Variable> displayedVars) {
        List<Variable> sorted = new ArrayList<>(displayedVars);
        sorted.sort((v1, v2) -> {
            // 1. OutputVariable named "y" first
            boolean v1IsY = v1 instanceof OutputVariable && "y".equals(v1.getName());
            boolean v2IsY = v2 instanceof OutputVariable && "y".equals(v2.getName());
            if (v1IsY && !v2IsY) return -1;
            if (!v1IsY && v2IsY) return 1;
            if (v1IsY && v2IsY) return 0;

            // 2. z[0-9]+ variables (WorkVariable, OutputVariable, InputVariable)
            String zPattern = "z(\\d+)";
            boolean v1IsZ = v1.getName().matches(zPattern) &&
                    (v1 instanceof WorkVariable || v1 instanceof OutputVariable || v1 instanceof engine.arguments.types.InputVariable);
            boolean v2IsZ = v2.getName().matches(zPattern) &&
                    (v2 instanceof WorkVariable || v2 instanceof OutputVariable || v2 instanceof engine.arguments.types.InputVariable);

            if (v1IsZ && !v2IsZ) return -1;
            if (!v1IsZ && v2IsZ) return 1;
            if (v1IsZ && v2IsZ) {
                // Sort by value descending, then by number after z descending
                int cmp = Integer.compare(v2.getValue(), v1.getValue());
                if (cmp != 0) return cmp;
                int n1 = Integer.parseInt(v1.getName().substring(1));
                int n2 = Integer.parseInt(v2.getName().substring(1));
                return Integer.compare(n2, n1); // Descending by number after z
            }

            // 3. All others: sort by name
            return v1.getName().compareTo(v2.getName());
        });

        displayedVars.clear();
        displayedVars.addAll(sorted);
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
        Program programToDebug = selectedProgram != null ? selectedProgram : (Program) s_emulator;
        programToDebug.prepareForDebugging();
        Command currentDebugCommand = programToDebug.getCurrentDebugCommand();
        instructionTableComponentController.setDebugHighlight(currentDebugCommand);
        executionPanelComponentController.updateDebugButtons();
    }

    public void stepOver() {
        Object selected = this.headerComponentController.getSelectedFunction();
        if (selected != null) {
            Program programToStepOver;
            if (selected.toString().equals(s_emulator.getCurrentProgramName())) {
                programToStepOver = (Program) s_emulator;
            } else {
                programToStepOver = null;
                for (Program sub : ((Program) s_emulator).getSunFunctions()) {
                    if (selected.toString().equals(sub.getCurrentProgramName())) {
                        programToStepOver = sub;
                        break;
                    }
                }
            }
            if (programToStepOver != null) {
                Map<String, Integer> prevValues = new HashMap<>();
                for (Variable v : programToStepOver.getVariables()) {
                    prevValues.put(v.getName(), v.getValue());
                }

                programToStepOver.stepOver();
                Command currentDebugCommand = programToStepOver.getCurrentDebugCommand();
                instructionTableComponentController.setDebugHighlight(currentDebugCommand);
                int currExpansionLvl = programToStepOver.getCurrentDegree();
                List<Command> displayedCommands = programToStepOver.getCommandsAtDesiredLevel(currExpansionLvl);

                Set<Variable> displayedVars = new LinkedHashSet<>();
                Set<Variable> inputVars = new LinkedHashSet<>();
                for (Command cmd : displayedCommands) {
                    Variable[] cmdVars = cmd.getAssociatedVariables();
                    if (cmdVars != null) {
                        if(cmd instanceof GotoLabel) { continue;}
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
                sortAllVars(displayedVars);

                Set<String> changedVars = new HashSet<>();
                for (Variable v : programToStepOver.getVariables()) {
                    Integer prev = prevValues.get(v.getName());
                    if (prev != null && prev != v.getValue()) {
                        changedVars.add(v.getName());
                    }
                }

                executionPanelComponentController.displayAllVars(displayedVars, changedVars);
                executionPanelComponentController.displayInputVars(inputVars, changedVars);
                executionPanelComponentController.setCyclesLabel(programToStepOver.getCycleSum());
                if(currentDebugCommand == null) {
                    isDebuggingEnabled = false;
                }
                executionPanelComponentController.updateDebugButtons();
            }
        }
    }

    public void stopDebugging() {
        Object selected = this.headerComponentController.getSelectedFunction();
        if (selected != null) {
            Program programToStepOver;
            if (selected.toString().equals(s_emulator.getCurrentProgramName())) {
                programToStepOver = (Program) s_emulator;
            } else {
                programToStepOver = null;
                for (Program sub : ((Program) s_emulator).getSunFunctions()) {
                    if (selected.toString().equals(sub.getCurrentProgramName())) {
                        programToStepOver = sub;
                        break;
                    }
                }
            }
            if (programToStepOver != null) {

                programToStepOver.reset();
                int currExpansionLvl = programToStepOver.getCurrentDegree();
                List<Command> displayedCommands = programToStepOver.getCommandsAtDesiredLevel(currExpansionLvl);

                Set<Variable> displayedVars = new LinkedHashSet<>();
                Set<Variable> inputVars = new LinkedHashSet<>();
                for (Command cmd : displayedCommands) {
                    if(cmd instanceof GotoLabel) { continue;}
                    Variable[] cmdVars = cmd.getAssociatedVariables();
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
                programToStepOver.reset();
                sortAllVars(displayedVars);

                executionPanelComponentController.displayAllVars(displayedVars, null);
                executionPanelComponentController.displayInputVars(inputVars, null);
                executionPanelComponentController.setCyclesLabel(programToStepOver.getCycleSum());
                executionPanelComponentController.setCyclesLabel(0);
                instructionTableComponentController.setDebugHighlight(null);
                isDebuggingEnabled = false;
                executionPanelComponentController.updateDebugButtons();
            }
        }
    }

    public void continueDebugging() {
        Object selected = this.headerComponentController.getSelectedFunction();
        if (selected != null) {
            Program programToContinue;
            if (selected.toString().equals(s_emulator.getCurrentProgramName())) {
                programToContinue = (Program) s_emulator;
            } else {
                programToContinue = null;
                for (Program sub : ((Program) s_emulator).getSunFunctions()) {
                    if (selected.toString().equals(sub.getCurrentProgramName())) {
                        programToContinue = sub;
                        break;
                    }
                }
            }
            if (programToContinue != null) {
                Map<String, Integer> prevValues = new HashMap<>();
                for (Variable v : programToContinue.getVariables()) {
                    prevValues.put(v.getName(), v.getValue());
                }
                while (isDebuggingEnabled && programToContinue.getCurrentDebugCommand() != null) {
                    programToContinue.stepOver();
                }
                // Clear highlight and update UI
                instructionTableComponentController.setDebugHighlight(null);
                executionPanelComponentController.updateDebugButtons();
                // Update variable tables
                Command currentDebugCommand = programToContinue.getCurrentDebugCommand();
                instructionTableComponentController.setDebugHighlight(currentDebugCommand);
                int currExpansionLvl = programToContinue.getCurrentDegree();
                List<Command> displayedCommands = programToContinue.getCommandsAtDesiredLevel(currExpansionLvl);

                Set<Variable> displayedVars = new LinkedHashSet<>();
                Set<Variable> inputVars = new LinkedHashSet<>();
                for (Command cmd : displayedCommands) {
                    Variable[] cmdVars = cmd.getAssociatedVariables();
                    if (cmdVars != null) {
                        if(cmd instanceof GotoLabel) { continue;}
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
                sortAllVars(displayedVars);

                Set<String> changedVars = new HashSet<>();
                for (Variable v : programToContinue.getVariables()) {
                    Integer prev = prevValues.get(v.getName());
                    if (prev != null && prev != v.getValue()) {
                        changedVars.add(v.getName());
                    }
                }

                executionPanelComponentController.displayAllVars(displayedVars, changedVars);
                executionPanelComponentController.displayInputVars(inputVars, changedVars);
                executionPanelComponentController.setCyclesLabel(programToContinue.getCycleSum());
            }
        }
        // Disable debugging
        isDebuggingEnabled = false;
        executionPanelComponentController.updateDebugButtons();
    }


    public void onFunctionSelectionChanged(String selectedName) {
        Program programToShow = null;
        if (selectedName.equals(s_emulator.getCurrentProgramName())) {
            programToShow = (Program) s_emulator;
            handleVarsForChangedFunction(programToShow);
        } else {
            for (Program sub : ((Program) s_emulator).getSunFunctions()) {
                if (selectedName.equals(sub.getCurrentProgramName())) {
                    programToShow = sub;
                    programToShow.expandCommands();
                    handleVarsForChangedFunction(programToShow);
                    break;
                }
            }
        }
        if (programToShow != null) {
            handleVarsForChangedFunction(programToShow);

            selectedProgram = programToShow; // Track the selected engine
            List<Command> displayedCommands = selectedProgram.getCommandsAtDesiredLevel(selectedProgram.getCurrentDegree());
            instructionTableComponentController.displayInstructions(displayedCommands);

            Set<Variable> displayedVars = new LinkedHashSet<>();
            Set<Variable> inputVars = new LinkedHashSet<>();
            for (Command cmd : displayedCommands) {
                Variable[] cmdVars = cmd.getAssociatedVariables();
                if (cmdVars != null) {
                    for (Variable v : cmdVars) {
                        if(cmd instanceof GotoLabel) { continue;}
                        if (v instanceof WorkVariable || v instanceof OutputVariable) {
                            displayedVars.add(v);
                        }
                        if (v instanceof engine.arguments.types.InputVariable) {
                            inputVars.add(v);
                        }
                    }
                }
            }
            selectedProgram.getVariables().forEach(v -> {
                if (v instanceof OutputVariable) {
                    displayedVars.add(v);
                }
            });

            executionPanelComponentController.displayAllVars(displayedVars);
            executionPanelComponentController.displayInputVars(inputVars, null);
            statsComponentController.updateStats(selectedProgram.getExecutionHistory());
        }
    }

    private void handleVarsForChangedFunction(Program programToShow) {
        if(programToShow.assosciatedProgram == null) {
            programToShow.hardReset();
            for(Program sub : programToShow.getSunFunctions()) {
                sub.hardReset();
            }
        } else {
            programToShow.hardReset();
            programToShow.assosciatedProgram.hardReset();
            for(Program sub : programToShow.getSunFunctions()) {
                sub.hardReset();
            }
        }
    }

    public void expandProgram(String functionName) {
        Program programToExpand;
        if (functionName.equals(s_emulator.getCurrentProgramName())) {
            programToExpand = (Program) s_emulator;
            handleVarsWhenChangingDegree(programToExpand);
        } else {
            programToExpand = null;
            for (Program sub : ((Program) s_emulator).getSunFunctions()) {
                if (functionName.equals(sub.getCurrentProgramName())) {
                    programToExpand = sub;
                    handleVarsWhenChangingDegree(sub);
                    break;
                }
            }
        }
        if (programToExpand != null) {
            handleVarsWhenChangingDegree(programToExpand);
            programToExpand.increaseDegree();
            int currExpansionLvl = programToExpand.getCurrentDegree();
            List<Command> displayedCommands = programToExpand.getCommandsAtDesiredLevel(currExpansionLvl);

            Set<Variable> displayedVars = new LinkedHashSet<>();
            Set<Variable> inputVars = new LinkedHashSet<>();
            for (Command cmd : displayedCommands) {
                if(cmd instanceof GotoLabel) { continue;}
                Variable[] cmdVars = cmd.getAssociatedVariables();
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
            programToExpand.getVariables().forEach(v -> {
                if (v instanceof OutputVariable) {
                    displayedVars.add(v);
                }
            });
            sortAllVars(displayedVars);
            instructionTableComponentController.displayInstructions(displayedCommands);
            executionPanelComponentController.displayAllVars(displayedVars);
            executionPanelComponentController.displayInputVars(inputVars, null);
        }
    }

    public void collapseProgram(String functionName) {
        Program programToCollapse;
        if (functionName.equals(s_emulator.getCurrentProgramName())) {
            programToCollapse = (Program) s_emulator;
            handleVarsWhenChangingDegree(programToCollapse);
        } else {
            programToCollapse = null;
            for (Program sub : ((Program) s_emulator).getSunFunctions()) {
                if (functionName.equals(sub.getCurrentProgramName())) {
                    programToCollapse = sub;
                    handleVarsWhenChangingDegree(sub);
                    break;
                }
            }
        }
        if (programToCollapse != null) {
            handleVarsWhenChangingDegree(programToCollapse);
            programToCollapse.decreaseDegree();
            int currExpansionLvl = programToCollapse.getCurrentDegree();
            List<Command> displayedCommands = programToCollapse.getCommandsAtDesiredLevel(currExpansionLvl);

            Set<Variable> displayedVars = new LinkedHashSet<>();
            Set<Variable> inputVars = new LinkedHashSet<>();
            for (Command cmd : displayedCommands) {
                if(cmd instanceof GotoLabel) { continue;}
                Variable[] cmdVars = cmd.getAssociatedVariables();
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
            programToCollapse.getVariables().forEach(v -> {
                if (v instanceof OutputVariable) {
                    displayedVars.add(v);
                }
            });
            sortAllVars(displayedVars);
            instructionTableComponentController.displayInstructions(displayedCommands);
            executionPanelComponentController.displayAllVars(displayedVars);
            executionPanelComponentController.displayInputVars(inputVars, null);
        }
    }

    private void handleVarsWhenChangingDegree(Program programToCollapse) {
        if(programToCollapse.assosciatedProgram == null) {
            programToCollapse.resetWorkAndOutputVariables();
            for(Program sub : programToCollapse.getSunFunctions()) {
                sub.resetWorkAndOutputVariables();
            }
        } else {
            programToCollapse.resetWorkAndOutputVariables();
            programToCollapse.assosciatedProgram.resetWorkAndOutputVariables();
            for(Program sub : programToCollapse.getSunFunctions()) {
                sub.resetWorkAndOutputVariables();
            }
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

    public String getProgramSummary() {
        Program programToSummarize = selectedProgram != null ? selectedProgram : (Program) s_emulator;
        if(programToSummarize == null) {
            return "No program currently loaded.";
        }
        String summary = "Program Name: " + programToSummarize.getCurrentProgramName() + "\nCurrent expansion level: " + programToSummarize.getCurrentDegree()+"\n";
        summary += "Number of Commands: " + programToSummarize.getCommandsAtDesiredLevel(programToSummarize.getCurrentDegree()).size() + "\n";
        summary += "Number of Basic Commands: " + programToSummarize.countBasicCommands() + "\n";
        summary += "Number of Synthetic Commands: " + programToSummarize.countSyntheticCommands() + "\n";
        return summary;
    }

    public void setCurrentDegree(String string, int degree) {
        Program programToSet = selectedProgram != null ? selectedProgram : (Program) s_emulator;
        if(programToSet != null) {
            while (programToSet.getCurrentDegree() < degree) {
                programToSet.increaseDegree();
            }
            while (programToSet.getCurrentDegree() > degree) {
                programToSet.decreaseDegree();
            }
            List<Command> displayedCommands = programToSet.getCommandsAtDesiredLevel(programToSet.getCurrentDegree());
            instructionTableComponentController.displayInstructions(displayedCommands);

            Set<Variable> displayedVars = new LinkedHashSet<>();
            Set<Variable> inputVars = new LinkedHashSet<>();
            for (Command cmd : displayedCommands) {
                Variable[] cmdVars = cmd.getAssociatedVariables();
                if (cmdVars != null) {
                    if(cmd instanceof GotoLabel) { continue;}
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
            programToSet.getVariables().forEach(v -> {
                if (v instanceof OutputVariable) {
                    displayedVars.add(v);
                }
            });
            sortAllVars(displayedVars);
            executionPanelComponentController.displayAllVars(displayedVars);
            executionPanelComponentController.displayInputVars(inputVars, null);
        }
    }

    public void newRunButtonPressed() {
        Object selected = this.headerComponentController.getSelectedFunction();
        if (selected != null) {
            Program programToExpand;
            if (selected.toString().equals(s_emulator.getCurrentProgramName())) {
                programToExpand = (Program) s_emulator;
            } else {
                programToExpand = null;
                for (Program sub : ((Program) s_emulator).getSunFunctions()) {
                    if (selected.toString().equals(sub.getCurrentProgramName())) {
                        programToExpand = sub;
                        break;
                    }
                }
            }
            if (programToExpand != null) {
                int currExpansionLvl = programToExpand.getCurrentDegree();
                List<Command> displayedCommands = programToExpand.getCommandsAtDesiredLevel(currExpansionLvl);

                Set<Variable> displayedVars = new LinkedHashSet<>();
                Set<Variable> inputVars = new LinkedHashSet<>();
                for (Command cmd : displayedCommands) {
                    Variable[] cmdVars = cmd.getAssociatedVariables();
                    if (cmdVars != null) {
                        if(cmd instanceof GotoLabel) { continue;}
                        for (Variable v : cmdVars) {
                            if (v instanceof WorkVariable || v instanceof OutputVariable) {
                                v.setValue(0);
                                displayedVars.add(v);
                            }
                            if (v instanceof engine.arguments.types.InputVariable) {
                                v.setValue(0);
                                inputVars.add(v);
                            }
                        }
                    }
                }
                programToExpand.getVariables().forEach(v -> {
                    if (v instanceof OutputVariable) {
                        displayedVars.add(v);
                    }
                });


                sortAllVars(displayedVars);
                stopDebugging();
                executionPanelComponentController.updateDebugButtons();
                executionPanelComponentController.clearAllVars();
                executionPanelComponentController.displayAllVars(displayedVars, null);
                executionPanelComponentController.displayInputVars(inputVars, null);
            }

        }
    }

    public HeaderController getheaderComponentController() {
        return this.headerComponentController;
    }

    public S_Emulator getEngine() {
        return this.s_emulator;
    }

    public ExecutionPanelController getExecutuionPanelComponent() {
        return this.executionPanelComponentController;
    }

    // import java.util.Objects;

    public void switchTheme(String themeName) {
        Platform.runLater(() -> {
            Scene scene = headerComponentController.CollapseButton.getScene();
            scene.getStylesheets().clear();

            switch (themeName) {
                case "Dark" ->
                        scene.getStylesheets().add(
                                Objects.requireNonNull(
                                        getClass().getResource("/utils/programs/EX02/dark-theme.css"),
                                        "dark-theme.css not found"
                                ).toExternalForm()
                        );
                case "Blue" ->
                        scene.getStylesheets().add(
                                Objects.requireNonNull(
                                        getClass().getResource("/utils/programs/EX02/blue-theme.css"),
                                        "blue-theme.css not found"
                                ).toExternalForm()
                        );
                default ->
                        scene.getStylesheets().add(
                                Objects.requireNonNull(
                                        getClass().getResource("/utils/programs/EX02/default-theme.css"),
                                        "default-theme.css not found"
                                ).toExternalForm()
                        );
            }
        });
    }


    private boolean animationsEnabled = true; // default: enabled

    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }
    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    public void playStartupAnimations(Scene scene) {
        if (!animationsEnabled) return;

        // Animation 1: Fade in the root node
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(1), scene.getRoot());
        fade.setFromValue(0);
        fade.setToValue(1);

        // Animation 2: Scale up the root node
        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.seconds(1), scene.getRoot());
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);

        // Animation 3: Rotate a logo or button (if exists)
        javafx.scene.Node logo = scene.lookup("#logo"); // Add fx:id="logo" to your logo node in FXML
        javafx.animation.RotateTransition rotate = null;
        if (logo != null) {
            rotate = new javafx.animation.RotateTransition(javafx.util.Duration.seconds(1), logo);
            rotate.setFromAngle(-30);
            rotate.setToAngle(0);
        }

        // Play animations in parallel
        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(fade, scale);
        if (rotate != null) pt.getChildren().add(rotate);
        pt.play();
    }
}
