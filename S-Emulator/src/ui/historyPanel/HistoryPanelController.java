// src/ui/historyPanel/HistoryPanelController.java
package ui.historyPanel;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import engine.commands.Command;
import ui.base.BaseController;

import java.util.ArrayList;
import java.util.List;

public class HistoryPanelController {
    @FXML
    private TableView<Command> historyTable;
    @FXML
    private TableColumn<Command, Number> idColumn;
    @FXML
    private TableColumn<Command, String> typeColumn;
    @FXML
    private TableColumn<Command, Number> cyclesColumn;
    @FXML
    private TableColumn<Command, String> labelColumn;
    @FXML
    private TableColumn<Command, String> instructionColumn;
    private BaseController mainController;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyIntegerWrapper(cd.getValue().getId()));
        typeColumn.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().getType()));
        cyclesColumn.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyIntegerWrapper(cd.getValue().getCycles()));
        labelColumn.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().getLabel()));
        instructionColumn.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().getCommandRepresentation()));
    }


    public void setMainController(BaseController baseController) {
        this.mainController = baseController;
    }

    public void displayParentChain(Command command) {
        List<Command> parentChain = new ArrayList<>();
        Command current = command;
        while (current != null) {
            parentChain.add(0, current);
            current = current.getParentCommand();
        }
        historyTable.getItems().setAll(parentChain.reversed());
    }
}