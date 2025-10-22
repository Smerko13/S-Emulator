package components.mainDashboard;

import components.api.HttpStatusUpdate;
import components.login.LoginController;
import components.mainDashboard.header.HeaderController;
import components.mainDashboard.users.UsersController;
import components.mainDashboard.ProgramsAndFunctions.ProgramsAndFunctionsController;
import components.shared.UserSession;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static util.Constants.*;

public class clientMainController implements Closeable, HttpStatusUpdate {
    private GridPane loginComponent;
    private LoginController logicController;
    @FXML private UsersController usersPanelController;
    @FXML private HeaderController headerPanelController;
    @FXML private ProgramsAndFunctionsController programsAndFunctionsPanelController;
    private final StringProperty currentUserName;
    private final UserSession userSession;

    public clientMainController() {
        currentUserName = new SimpleStringProperty(JHON_DOE);
        userSession = UserSession.getInstance();
    }

    @FXML
    public void initialize() {
        usersPanelController.setMainController(this);
        usersPanelController.startUsersAutoRefresh();
        headerPanelController.setMainController(this);

        // Initialize Programs and Functions panel
        if (programsAndFunctionsPanelController != null) {
            programsAndFunctionsPanelController.setMainController(this);
        }

        // Sync local userName with shared session
        currentUserName.bindBidirectional(userSession.userNameProperty());
    }

    public void updateUserName(String userName) {
        userSession.setUserName(userName);
        headerPanelController.updateUserName(userName);
    }

    @Override
    public void close() {
        if (usersPanelController != null) {
            usersPanelController.stopUsersAutoRefresh();
        }
        if (programsAndFunctionsPanelController != null) {
            programsAndFunctionsPanelController.stopAutoRefresh();
        }
    }


    private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            logicController = fxmlLoader.getController();
            logicController.setClientMainController(this);
            //setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void updateHttpLine(String line) {
        //httpStatusComponentController.addHttpStatusLine(line);
    }


    public void switchToLogin() {
        Platform.runLater(userSession::clearSession);
    }

    public void onLoginSuccess(String userName) {
        updateUserName(userName);              // updates header, etc.
        if (usersPanelController != null) {
            usersPanelController.startUsersAutoRefresh();  // begins polling /userslist
        }
    }



    public boolean sendFileToServerForValidation(File selectedFile) {
        if (selectedFile == null || !selectedFile.exists()) {
            showUploadError("File not found", "The selected file does not exist.");
            return false;
        }

        if (!selectedFile.getName().toLowerCase().endsWith(".xml")) {
            showUploadError("Invalid file type", "Only XML files are allowed.");
            return false;
        }

        // Show upload progress
        Platform.runLater(() -> {
            if (headerPanelController != null) {
                headerPanelController.setUploadStatus("Uploading file...");
            }
        });

        // Perform asynchronous upload using the multipart HTTP client utility
        util.http.HttpClientUtil.runAsyncMultipartPost(UPLOAD, selectedFile, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Platform.runLater(() -> {
                    showUploadError("Upload Failed", "Network error: " + e.getMessage());
                    if (headerPanelController != null) {
                        headerPanelController.setUploadStatus("Upload failed");
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        handleUploadSuccess(responseBody, selectedFile.getName());
                    } else {
                        handleUploadError(response.code(), responseBody);
                    }
                });
            }
        });

        return true; // Indicates upload was initiated successfully
    }

    private void handleUploadSuccess(String responseBody, String fileName) {
        try {
            // Simple JSON parsing to extract success information
            if (responseBody.contains("\"success\":true")) {
                String message = "File uploaded successfully!";

                // Extract program name if available
                if (responseBody.contains("\"programName\":")) {
                    int start = responseBody.indexOf("\"programName\":\"") + 15;
                    int end = responseBody.indexOf("\"", start);
                    if (end > start) {
                        String programName = responseBody.substring(start, end);
                        message = "Program '" + programName + "' uploaded successfully!";
                    }
                }

                showUploadSuccess("Upload Successful", message);

                if (headerPanelController != null) {
                    headerPanelController.setUploadStatus("Upload completed: " + fileName);
                }

                // Refresh the programs list to show the new program
                refreshProgramsList();

            } else {
                showUploadError("Upload Failed", "Server did not confirm successful upload.");
            }
        } catch (Exception e) {
            showUploadError("Response Error", "Error processing server response: " + e.getMessage());
        }
    }

    private void handleUploadError(int statusCode, String responseBody) {
        String errorMessage = "Upload failed";

        try {
            // Extract error message from JSON response
            if (responseBody.contains("Validation failed:")) {
                int start = responseBody.indexOf("Validation failed:");
                int end = responseBody.indexOf("\"", start);
                if (end > start) {
                    errorMessage = responseBody.substring(start, end);
                }
            } else if (responseBody.contains("\"")) {
                // Extract any quoted error message
                int start = responseBody.indexOf("\"") + 1;
                int end = responseBody.lastIndexOf("\"");
                if (end > start) {
                    errorMessage = responseBody.substring(start, end);
                }
            }
        } catch (Exception e) {
            errorMessage = "Upload failed with status code: " + statusCode;
        }

        showUploadError("Upload Failed", errorMessage);

        if (headerPanelController != null) {
            headerPanelController.setUploadStatus("Upload failed");
        }
    }

    private void showUploadSuccess(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showUploadError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void refreshProgramsList() {
        // This would trigger a refresh of the programs panel to show newly uploaded programs
        // Implementation depends on how the programs panel is structured
    }


    // Credits management methods
    public int getUserCredits() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FULL_SERVER_PATH + "/credits?userId=" + getCurrentUserId()))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                // Simple JSON parsing for credits
                if (responseBody.contains("\"credits\":")) {
                    String creditsStr = responseBody.substring(
                            responseBody.indexOf("\"credits\":") + 10,
                            responseBody.indexOf("}")
                    );
                    int credits = Integer.parseInt(creditsStr);
                    // Update shared session with latest credits from server
                    userSession.setCredits(credits);
                    return credits;
                }
            }
        } catch (Exception e) {
        }
        return userSession.getCredits(); // Return cached value if server call fails
    }

    public boolean addUserCredits(int creditsToAdd) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FULL_SERVER_PATH + "/credits?userId=" + getCurrentUserId() + "&credits=" + creditsToAdd))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Refresh credits from server to get updated value
                getUserCredits();
                return true;
            }
            return false;

        } catch (Exception e) {
            return false;
        }
    }


    private String getCurrentUserId() {
        return userSession.getUserName().replaceAll("\\s+", "_").toLowerCase();
    }

}
