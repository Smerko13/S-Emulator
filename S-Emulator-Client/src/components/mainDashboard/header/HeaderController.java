package components.mainDashboard.header;

import components.mainDashboard.clientMainController;
import components.shared.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class HeaderController {
    @FXML private TextField creditInputTextField;
    @FXML private Button chargeCreditsButton;
    @FXML private TextField filePathTextField;
    @FXML private Button loadFileButton;
    @FXML private Label creditsLabel;
    @FXML private Label userNameLabel;
    clientMainController mainController;
    private Timer creditsRefreshTimer;
    private final UserSession userSession;

    public HeaderController() {
        userSession = UserSession.getInstance();
    }

    public void setMainController(clientMainController mainController) {
        this.mainController = mainController;
        startCreditsAutoRefresh();

        // Bind UI components to shared session
        userSession.userNameProperty().addListener((obs, oldVal, newVal) ->
            Platform.runLater(() -> userNameLabel.setText("User Name: " + newVal)));
        userSession.creditsProperty().addListener((obs, oldVal, newVal) ->
            Platform.runLater(() -> creditsLabel.setText("Credits: " + newVal)));

        // Initialize with current values
        updateUserName(userSession.getUserName());
        updateCreditsDisplay();
    }

    public void updateUserName(String userName) {
        userNameLabel.setText("User Name: " + userName);
        updateCreditsDisplay();
    }

    private void startCreditsAutoRefresh() {
        creditsRefreshTimer = new Timer(true);
        creditsRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateCreditsDisplay();
            }
        }, 1000, 3000); // Update every 3 seconds
    }

    public void updateCreditsDisplay() {
        if (mainController != null) {
            Platform.runLater(() -> {
                int credits = mainController.getUserCredits();
                creditsLabel.setText("Credits: " + credits);
            });
        }
    }

    @FXML
    public void chargeCreditsButtonPressed(ActionEvent actionEvent) {
        String creditsText = creditInputTextField.getText();

        if (creditsText == null || creditsText.trim().isEmpty()) {
            showAlert("Invalid Input", "Please enter the number of credits to add.");
            return;
        }

        try {
            int creditsToAdd = Integer.parseInt(creditsText.trim());

            if (creditsToAdd <= 0) {
                showAlert("Invalid Amount", "Credits amount must be a positive number.");
                return;
            }

            // Add credits through main controller
            if (mainController.addUserCredits(creditsToAdd)) {
                creditInputTextField.clear();
                updateCreditsDisplay();
                showSuccessAlert("Credits Added", "Successfully added " + creditsToAdd + " credits!");
            } else {
                showAlert("Error", "Failed to add credits. Please try again.");
            }

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number.");
        }
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

        // If a file was selected, upload it directly (program name will be extracted from XML)
        if (selectedFile != null) {
            if (this.mainController.sendFileToServerForValidation(selectedFile)) {
                filePathTextField.setText(selectedFile.getAbsolutePath());
            } else {
                filePathTextField.setText("Invalid XML file. Please select a valid file.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        if (creditsRefreshTimer != null) {
            creditsRefreshTimer.cancel();
        }
    }
}
