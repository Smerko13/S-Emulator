// src/ui/historyPanel/HistoryPanelController.java
package ui.historyPanel;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import engine.commands.Command;
import ui.base.BaseController;

import java.util.ArrayList;
import java.util.List;

public class HistoryPanelController {
    @FXML
    private TextFlow textFlow;
    private BaseController mainController;

    public void setMainController(BaseController baseController) {
        this.mainController = baseController;
    }

    public void displayParentChain(Command command) {
        textFlow.getChildren().clear();
        if (command == null) return;

        // Collect the chain from root to selected
        List<Command> chain = new ArrayList<>();
        Command curr = command;
        while (curr != null) {
            chain.add(0, curr); // insert at start to reverse order
            curr = curr.getParentCommand();
        }

        // Display each command in order, with arrows
        for (int i = 0; i < chain.size(); i++) {
            Command cmd = chain.get(i);
            textFlow.getChildren().add(new Text(cmd.getCommandRepresentation()));
            if (i < chain.size() - 1) {
                textFlow.getChildren().add(new Text("\n\u2193\n")); // Down arrow
            }
        }
    }
}