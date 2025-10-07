package components.mainDashboard.header;

import components.mainDashboard.clientMainController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class HeaderController {
    @FXML private TextField creditInputTextField;
    @FXML private Button chargeCreditsButton;
    @FXML private TextField filePathTextField;
    @FXML private Button loadFileButton;
    @FXML private Label creditsLabel;
    @FXML private Label userNameLabel;
    clientMainController mainController;

    public void setMainController(clientMainController mainController) {
        this.mainController = mainController;
    }

    public void updateUserName(String userName) {
        userNameLabel.setText("User Name: " + userName);
    }
}
