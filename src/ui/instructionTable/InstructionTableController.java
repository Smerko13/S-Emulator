package ui.instructionTable;

import engine.commands.Command;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;

import javafx.scene.control.TableView;
import javafx.scene.text.TextFlow;
import ui.base.BaseController;
import java.util.List;


public class InstructionTableController {
    @FXML
    private TextFlow SummaryLineTextBox;
    private BaseController mainController;
    @FXML
    private TableView<Command> instructionTableView;
    @FXML
    private TableColumn<Command, Number> idColumn;
    @FXML
    private TableColumn<Command, String> typeColumn;
    @FXML
    private TableColumn<Command, Number> cyclesColumn;
    @FXML
    private TableColumn<Command, String> instructionColumn;
    @FXML
    private TableColumn<Command, String> labelColumn;
    private Object currentHighlight = null;
    private final ObservableList<Command> commands = FXCollections.observableArrayList();
    private Command selectedCommand = null;
    private Command debugHighlight = null;

    public void setMainController(BaseController baseController) {
        this.mainController = baseController;
    }

    @FXML
    private void initialize() {
        instructionTableView.setItems(commands);
        idColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty ? null : Integer.toString(getIndex() + 1));
            }
        });
        idColumn.setSortable(false);

        typeColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(String.valueOf(cd.getValue().getType())));
        cyclesColumn.setCellValueFactory(cd ->
                new ReadOnlyIntegerWrapper(cd.getValue().getCycles()));
        instructionColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getCommandRepresentation()));
        labelColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getLabel() == null ? "" : cd.getValue().getLabel()));

        instructionTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Command item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item == debugHighlight) {
                        setStyle("-fx-background-color: red;");
                } else if (item == selectedCommand) {
                    setStyle("-fx-background-color: lightblue;");
                } else if (currentHighlight != null && !"None".equals(currentHighlight)) {
                    String highlightStr = currentHighlight.toString().trim();
                    boolean highlight = false;
                    if ((item.getLabel() != null && item.getLabel().trim().equals(highlightStr)) ||
                            (item.getCommandRepresentation() != null && item.getCommandRepresentation().contains(highlightStr))) {
                        highlight = true;
                    }
                    for (var v : item.getAssociatedVariables()) {
                        if (v != null && v.getName().equals(currentHighlight)) {
                            highlight = true;
                        }
                    }
                    setStyle(highlight ? "-fx-background-color: yellow;" : "");
                } else {
                    setStyle("");
                }
            }
        });

        instructionTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedCommand = newSel;
            if (newSel != null) {
                SummaryLineTextBox.getChildren().clear();
                SummaryLineTextBox.getChildren().add(
                        new javafx.scene.text.Text(newSel.getCommandRepresentation() + "\n" +
                                "Type: " + newSel.getType() + "\n" +
                                "Cycles: " + newSel.getCycles() + "\n" +
                                "Label: " + (newSel.getLabel() == null ? "" : newSel.getLabel()))
                );
            } else {
                SummaryLineTextBox.getChildren().clear();
            }
            instructionTableView.refresh();

            // Notify main controller to update history panel
            if (mainController != null) {
                mainController.onHighlightSelectionChanged(newSel);
            }
        });


        instructionTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedCommand = newSel;
            if (newSel != null) {
                SummaryLineTextBox.getChildren().clear();
                SummaryLineTextBox.getChildren().add(
                        new javafx.scene.text.Text(newSel.getCommandRepresentation() + "\n" +
                                "Command: " + newSel.getCommandName() + "\n" +
                                "Type: " + newSel.getType() + "\n" +
                                "Cycles: " + newSel.getCycles() + "\n" +
                                "Associated Variables: " + String.join(", ", newSel.getUsedVariableNames()) + "\n" +
                                "Label: " + (newSel.getLabel() == null ? "" : newSel.getLabel()))
                );
            } else {
                SummaryLineTextBox.getChildren().clear();
            }
            instructionTableView.refresh();
        });
    }

    public void displayInstructions(List<Command> commands) {
        this.commands.setAll(commands == null ? List.of() : commands);
        instructionTableView.refresh();
        mainController.setMonitors();
    }


    public void setHighlight(Object highlight) {
        if (highlight instanceof engine.commands.Command) {
            this.currentHighlight = null;
        } else {
            this.currentHighlight = highlight;
        }
        instructionTableView.refresh();
    }

    public void setDebugHighlight(Command currentDebugCommand) {
        this.debugHighlight = currentDebugCommand;
        instructionTableView.refresh();
    }
}
