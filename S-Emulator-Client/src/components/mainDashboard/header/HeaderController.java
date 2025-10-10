package components.mainDashboard.header;

import components.mainDashboard.clientMainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

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

        // If a file was selected, get program name from user
        if (selectedFile != null) {
            String programName = getProgramNameFromUser(selectedFile.getName());

            if (programName != null && !programName.trim().isEmpty()) {
                if (this.mainController.sendFileToServerForValidation(selectedFile, programName)) {
                    filePathTextField.setText(selectedFile.getAbsolutePath() + " (Program: " + programName + ")");
                } else {
                    filePathTextField.setText("Invalid XML file. Please select a valid file.");
                }
            } else {
                filePathTextField.setText("Upload cancelled - program name required.");
            }
        }
    }

    private String getProgramNameFromUser(String fileName) {
        // Create a dialog to get program name from user
        TextInputDialog dialog = new TextInputDialog(fileName.replace(".xml", ""));
        dialog.setTitle("Program Name");
        dialog.setHeaderText("Enter a name for this program:");
        dialog.setContentText("Program name:");

        // Show dialog and get result
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
}
