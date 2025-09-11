package ui.instructionTable;

import engine.commands.Command;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
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
    }

    /** Call this after loading the program */
    public void displayInstructions(List<Command> commands) {
        this.commands.setAll(commands == null ? List.of() : commands);
        instructionTableView.refresh(); // optional
    }
}
