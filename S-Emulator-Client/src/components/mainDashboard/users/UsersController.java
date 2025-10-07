package components.mainDashboard.users;

import components.clientMainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class UsersController {
    @FXML private TableColumn executionNumberColumn;
    @FXML private TableView statsTable;
    @FXML private TableColumn expansionLevelColumn;
    @FXML private TableColumn cyclesColumn;
    @FXML private TableColumn runTypeColumn;
    @FXML private TableColumn nameColumn;
    @FXML private TableColumn architectureColumn;
    @FXML private TableColumn outputColumn;
    @FXML private Button showStatusButton;
    @FXML private Button reRunButton;
    @FXML private Button unselectUserButton;
    @FXML private TableColumn userNameColumn;
    @FXML private TableColumn programCountColumn;
    @FXML private TableColumn FunctionCountColumn;
    @FXML private TableColumn creditsColumn;
    @FXML private TableColumn creditsUsedColumn;
    @FXML private TableColumn executionsColumn;
    clientMainController mainController;

    public void setMainController(clientMainController mainController) {
        this.mainController = mainController;
    }

    public void reRunButtonPressed(ActionEvent actionEvent) {
    }

    public void showStatusButtonPressed(ActionEvent actionEvent) {
    }

    public void unselectedUserPressed(ActionEvent actionEvent) {
    }
}
