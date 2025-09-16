package ui.base;

import engine.Engine;
import engine.S_Emulator;
import engine.arguments.Variable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import javafx.fxml.FXML;
import ui.executionPanelController.ExecutionPanelController;
import ui.header.HeaderController;
import ui.historyPanel.HistoryPanelController;
import ui.instructionTable.InstructionTableController;

import java.io.File;
import java.util.*;

public class BaseController {
    @FXML private HeaderController headerComponentController;
    @FXML private InstructionTableController instructionTableComponentController;
    @FXML private ExecutionPanelController executionPanelComponentController;
    @FXML private HistoryPanelController historyPanelComponentController;
    S_Emulator s_emulator;
    List<S_Emulator> programHistory;

    @FXML
    public void initialize() {
        if(headerComponentController != null
                && instructionTableComponentController != null
                && executionPanelComponentController != null
                && historyPanelComponentController != null) {
            executionPanelComponentController.setMainController(this);
            headerComponentController.setMainController(this);
            instructionTableComponentController.setMainController(this);
            historyPanelComponentController.setMainController(this);
        }
        s_emulator = new Engine();
        programHistory = new ArrayList<>();
    }


    public void loadFile(File selectedFile) {
        s_emulator = new Engine();
        programHistory.add(s_emulator);
        boolean fileLoadedSuccessfully = s_emulator.readProgramFromXml(selectedFile.toString());
        if (fileLoadedSuccessfully) {
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
            executionPanelComponentController.displayAllVars(displayedVars);
            executionPanelComponentController.displayInputVars(inputVars);
        } else {
            System.out.println("WRONG FILE");
        }
    }

    public String getCurrentDegree() {
        return s_emulator.getCurrentDegree() + "";
    }

    public String getMaxDegree() {
        return String.valueOf(s_emulator.getMaxExpansionDepth());
    }

    public void expandProgram() {
        s_emulator.increaseDegree();
        int currExpansionLvl = s_emulator.getCurrentDegree();
        List<Command> displayedCommands = s_emulator.getCommandsAtDesiredLevel(currExpansionLvl);
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
        executionPanelComponentController.displayAllVars(displayedVars);
        executionPanelComponentController.displayInputVars(inputVars);
    }

    public void collapseProgram() {
        s_emulator.decreaseDegree();
        int currExpansionLvl = s_emulator.getCurrentDegree();
        List<Command> displayedCommands = s_emulator.getCommandsAtDesiredLevel(currExpansionLvl);
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
        executionPanelComponentController.displayAllVars(displayedVars);
        executionPanelComponentController.displayInputVars(inputVars);
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
        // Run the program
        this.s_emulator.executeProgram(s_emulator.getCurrentDegree());

        // Get all variables from the engine (source of truth)
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
        // Refresh tables with up-to-date variable instances
        //executionPanelComponentController.displayAllVars(displayedVars);
        executionPanelComponentController.displayVarsForCurrentInstructions(
                s_emulator.getVariables(),
                s_emulator.getCommandsAtDesiredLevel(s_emulator.getCurrentDegree())
        );
        executionPanelComponentController.displayInputVars(inputVars);
        executionPanelComponentController.setCyclesLabel(s_emulator.getCycleSum());
    }
}
