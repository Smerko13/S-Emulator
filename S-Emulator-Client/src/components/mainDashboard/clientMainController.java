package components.mainDashboard;

import components.api.HttpStatusUpdate;
import components.login.LoginController;
import components.mainDashboard.header.HeaderController;
import components.mainDashboard.users.UsersController;
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
        try {
            // Read the XML content
            String xmlContent = Files.readString(selectedFile.toPath(), StandardCharsets.UTF_8);
            System.out.println("CLIENT - XML Content Length: " + xmlContent.length());
            System.out.println("CLIENT - First 200 chars: " + xmlContent.substring(0, Math.min(200, xmlContent.length())));

            // Check byte array size
            byte[] xmlBytes = xmlContent.getBytes(StandardCharsets.UTF_8);
            System.out.println("CLIENT - XML bytes length: " + xmlBytes.length);
            System.out.println("CLIENT - Current User ID: " + getCurrentUserId());

            // Create HTTP client and request
            HttpClient client = HttpClient.newHttpClient();

            // Build the request with XML content in the body - program name will be extracted from XML on server
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FULL_SERVER_PATH + "/" + VALIDATION_ENDPOINT + "?userId=" + getCurrentUserId()))
                    .header("Content-Type", "application/xml; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(xmlContent, StandardCharsets.UTF_8))
                    .build();

            System.out.println("CLIENT - Sending request to: " + request.uri());
            System.out.println("CLIENT - Request headers: " + request.headers().map());

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CLIENT - Response status: " + response.statusCode());
            System.out.println("CLIENT - Response body: " + response.body());
            System.out.println("CLIENT - Response headers: " + response.headers().map());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.out.println("CLIENT - Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
            System.err.println("Error fetching credits: " + e.getMessage());
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

            System.out.println("Add credits response: " + response.statusCode() + " - " + response.body());
            if (response.statusCode() == 200) {
                // Refresh credits from server to get updated value
                getUserCredits();
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error adding credits: " + e.getMessage());
            return false;
        }
    }


    private String getCurrentUserId() {
        return userSession.getUserName().replaceAll("\\s+", "_").toLowerCase();
    }

}
