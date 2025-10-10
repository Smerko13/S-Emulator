package components.mainDashboard.header;

import components.mainDashboard.clientMainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

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

    public void loadFileButtonPressed(ActionEvent actionEvent) {
        // Create a FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML File");

        // Set extension filter for XML files
        FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(xmlFilter);

        // Set initial directory to user's documents folder
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        // Get the current stage from the button
        Stage stage = (Stage) loadFileButton.getScene().getWindow();

        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(stage);

        // If a file was selected, update the file path text field
        if (selectedFile != null) {
            filePathTextField.setText(selectedFile.getAbsolutePath());
        }
    }
}
