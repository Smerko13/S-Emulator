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
import ui.base.BaseController;
import java.util.List;


public class InstructionTableController {
    private BaseController mainController;
    @FXML
    private TableView<Command> instructionTableView;
    @FXML private TableColumn<Command, Number> idColumn;
    @FXML private TableColumn<Command, String> typeColumn;
    @FXML private TableColumn<Command, Number> cyclesColumn;
    @FXML private TableColumn<Command, String> instructionColumn;
    @FXML private TableColumn<Command, String> labelColumn;
    private Object currentHighlight = null;

    private final ObservableList<Command> commands = FXCollections.observableArrayList();



    public void setMainController(BaseController baseController) {
        this.mainController = baseController;
    }
    @FXML
    private void initialize() {
        instructionTableView.setItems(commands);
        idColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
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
                if (empty || item == null || currentHighlight == null || "None".equals(currentHighlight)) {
                    setStyle("");
                } else {
                    boolean highlight = false;
                    String highlightStr = currentHighlight.toString().trim();
                    // Highlight if label matches
                    if (item.getLabel() != null && item.getLabel().trim().equals(highlightStr)) {
                        highlight = true;
                    }
                    // Highlight if instruction contains the label
                    if (item.getCommandRepresentation() != null &&
                            item.getCommandRepresentation().contains(highlightStr)) {
                        highlight = true;
                    }
                    // Highlight if variable matches
                    for (var v : item.getAssociatedVariables()) {
                        if (v != null && v.getName().equals(currentHighlight)) {
                            highlight = true;
                        }
                    }
                    setStyle(highlight ? "-fx-background-color: yellow;" : "");
                }
            }
        });
    }

    public void displayInstructions(List<Command> commands) {
        this.commands.setAll(commands == null ? List.of() : commands);
        instructionTableView.refresh();
        mainController.setMonitors();
    }


    public void setHighlight(Object highlight) {
        this.currentHighlight = highlight;
        instructionTableView.refresh();
    }

}
