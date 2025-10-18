package components.executionDashboard.instructionPanel;

import api.dto.Architecture;
import api.dto.InstructionDTO;
import components.executionDashboard.ExecutionDashboardController;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;

public class InstructionTableController {

    @FXML private TableView<InstructionDTO> instructionTableView;
    @FXML private TableColumn<InstructionDTO, Number> idColumn;
    @FXML private TableColumn<InstructionDTO, String> typeColumn;
    @FXML private TableColumn<InstructionDTO, Number> cyclesColumn;
    @FXML private TableColumn<InstructionDTO, String> labelColumn;
    @FXML private TableColumn<InstructionDTO, String> instructionColumn;
    @FXML private TableColumn<InstructionDTO, String> architectureColumn;

    @FXML private TextFlow SummaryLineTextBox;
    @FXML private Label architectureSummaryLabel;

    private ExecutionDashboardController parent;

    private final ObservableList<InstructionDTO> rows = FXCollections.observableArrayList();
    private Integer debugHighlightId = null;
    private List<Integer> incompatibleInstructionIds = null;

    @FXML
    private void initialize() {
        instructionTableView.setItems(rows);

        // Use DTO fields (no getIndex() here)
        idColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getId()));
        typeColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getType()));
        cyclesColumn.setCellValueFactory(cd -> new ReadOnlyIntegerWrapper(cd.getValue().getCycles()));
        labelColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getLabel() == null ? "" : cd.getValue().getLabel()));
        instructionColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getText()));

        // Architecture column - shows required architecture for each instruction
        if (architectureColumn != null) {
            architectureColumn.setCellValueFactory(cd -> {
                InstructionDTO instruction = cd.getValue();
                if (instruction.getRequiredArchitecture() != null) {
                    return new ReadOnlyStringWrapper(instruction.getRequiredArchitecture().getDisplayName());
                }
                return new ReadOnlyStringWrapper("Generation I"); // Default to cheapest
            });
        }

        // Row highlighting for the debug instruction id (if server provides it)
        instructionTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InstructionDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (incompatibleInstructionIds != null && incompatibleInstructionIds.contains(item.getId())) {
                    // Incompatible architecture - highlight in RED
                    setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold; -fx-border-color: #d32f2f; -fx-border-width: 0 0 0 4;");
                } else if (debugHighlightId != null && item.getId() == debugHighlightId) {
                    // Debug highlight - yellow/orange
                    setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                } else {
                    setStyle("");
                }
            }
        });

        // Show a small summary of the selected instruction
        instructionTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, cur) -> {
            SummaryLineTextBox.getChildren().clear();
            if (cur != null) {
                String archInfo = cur.getRequiredArchitecture() != null
                    ? cur.getRequiredArchitecture().getDisplayName()
                    : "Generation I";

                SummaryLineTextBox.getChildren().add(new Text(
                        cur.getText() + "\n" +
                                "Type: " + cur.getType() + "\n" +
                                "Cycles: " + cur.getCycles() + "\n" +
                                "Architecture: " + archInfo + "\n" +
                                "Label: " + (cur.getLabel() == null ? "" : cur.getLabel())
                ));

                // Fetch and display parent command chain in history panel
                if (parent != null) {
                    parent.fetchParentCommandChain(cur.getId());
                }
            }
        });
    }

    /** Called by ExecutionDashboardController */
    public void setInstructions(List<InstructionDTO> list, Integer highlightedId) {
        rows.setAll(list == null ? List.of() : list);
        this.debugHighlightId = highlightedId;
        instructionTableView.refresh();

        // Update architecture summary
        updateArchitectureSummary(list);
    }

    /**
     * Update the architecture summary bar showing command counts per architecture
     */
    private void updateArchitectureSummary(List<InstructionDTO> instructions) {
        if (architectureSummaryLabel == null || instructions == null || instructions.isEmpty()) {
            return;
        }

        // Count commands by architecture
        int gen1Count = 0, gen2Count = 0, gen3Count = 0, gen4Count = 0;

        for (InstructionDTO instruction : instructions) {
            Architecture arch = instruction.getRequiredArchitecture();
            if (arch == null) {
                gen1Count++; // Default to Generation I
            } else {
                switch (arch) {
                    case GENERATION_I:
                        gen1Count++;
                        break;
                    case GENERATION_II:
                        gen2Count++;
                        break;
                    case GENERATION_III:
                        gen3Count++;
                        break;
                    case GENERATION_IV:
                        gen4Count++;
                        break;
                }
            }
        }

        // Build summary text
        StringBuilder summary = new StringBuilder("Architecture Summary: ");
        summary.append("Gen I: ").append(gen1Count).append(" | ");
        summary.append("Gen II: ").append(gen2Count).append(" | ");
        summary.append("Gen III: ").append(gen3Count).append(" | ");
        summary.append("Gen IV: ").append(gen4Count);

        architectureSummaryLabel.setText(summary.toString());

        System.out.println("Architecture summary updated: " + summary);
    }

    /**
     * Highlight instructions that are incompatible with the selected architecture
     */
    public void highlightIncompatibleInstructions(List<Integer> instructionIds) {
        System.out.println("InstructionTableController: Highlighting " +
            (instructionIds != null ? instructionIds.size() : 0) + " incompatible instructions");
        this.incompatibleInstructionIds = instructionIds;
        instructionTableView.refresh();
    }

    /**
     * Clear incompatible instruction highlighting
     */
    public void clearIncompatibleHighlighting() {
        this.incompatibleInstructionIds = null;
        instructionTableView.refresh();
    }

    /**
     * Validate if all instructions are compatible with selected architecture
     * Returns the highest required architecture if incompatible, null if all compatible
     */
    public Architecture validateArchitectureCompatibility(Architecture selectedArchitecture) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        Architecture highestRequired = Architecture.GENERATION_I;
        List<Integer> incompatible = new java.util.ArrayList<>();

        for (InstructionDTO instruction : rows) {
            Architecture required = instruction.getRequiredArchitecture();
            if (required == null) {
                required = Architecture.GENERATION_I;
            }

            // Check if this instruction requires a higher architecture than selected
            if (required.getCost() > selectedArchitecture.getCost()) {
                incompatible.add(instruction.getId());

                // Track the highest required architecture
                if (required.getCost() > highestRequired.getCost()) {
                    highestRequired = required;
                }
            }
        }

        // Highlight incompatible instructions
        if (!incompatible.isEmpty()) {
            highlightIncompatibleInstructions(incompatible);
            return highestRequired;
        } else {
            clearIncompatibleHighlighting();
            return null;
        }
    }

    public void setMainController(ExecutionDashboardController parent) {
        this.parent = parent;
    }

    /**
     * Handler for the "Show Program Summary" button
     */
    @FXML
    private void ShowProgramSummary(ActionEvent event) {
        System.out.println("Show Program Summary button clicked");

        // Generate and display a summary of the program
        if (rows == null || rows.isEmpty()) {
            SummaryLineTextBox.getChildren().clear();
            SummaryLineTextBox.getChildren().add(new Text("No instructions to summarize."));
            return;
        }

        // Count commands by architecture
        int gen1Count = 0, gen2Count = 0, gen3Count = 0, gen4Count = 0;
        int totalCycles = 0;

        for (InstructionDTO instruction : rows) {
            Architecture arch = instruction.getRequiredArchitecture();
            if (arch == null) {
                gen1Count++;
            } else {
                switch (arch) {
                    case GENERATION_I:
                        gen1Count++;
                        break;
                    case GENERATION_II:
                        gen2Count++;
                        break;
                    case GENERATION_III:
                        gen3Count++;
                        break;
                    case GENERATION_IV:
                        gen4Count++;
                        break;
                }
            }
            totalCycles += instruction.getCycles();
        }

        // Determine minimum required architecture
        Architecture minRequired = Architecture.GENERATION_I;
        if (gen4Count > 0) {
            minRequired = Architecture.GENERATION_IV;
        } else if (gen3Count > 0) {
            minRequired = Architecture.GENERATION_III;
        } else if (gen2Count > 0) {
            minRequired = Architecture.GENERATION_II;
        }

        // Build summary text
        StringBuilder summary = new StringBuilder();
        summary.append("=== PROGRAM SUMMARY ===\n\n");
        summary.append("Total Instructions: ").append(rows.size()).append("\n");
        summary.append("Total Cycles: ").append(totalCycles).append("\n\n");
        summary.append("Instructions by Architecture:\n");
        summary.append("  Generation I:   ").append(gen1Count).append("\n");
        summary.append("  Generation II:  ").append(gen2Count).append("\n");
        summary.append("  Generation III: ").append(gen3Count).append("\n");
        summary.append("  Generation IV:  ").append(gen4Count).append("\n\n");
        summary.append("Minimum Required Architecture: ").append(minRequired.getDisplayName()).append("\n");
        summary.append("Base Cost: ").append(minRequired.getCost()).append(" credits\n");
        summary.append("Estimated Total Cost: ").append(minRequired.getCost() + totalCycles).append(" credits");

        // Display in the summary text box
        SummaryLineTextBox.getChildren().clear();
        SummaryLineTextBox.getChildren().add(new Text(summary.toString()));

        System.out.println("Program summary displayed: " + rows.size() + " instructions, min arch: " + minRequired.name());
    }
}
