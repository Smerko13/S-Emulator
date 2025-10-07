package components.mainDashboard;

import components.api.HttpStatusUpdate;
import components.login.LoginController;
import components.mainDashboard.header.HeaderController;
import components.mainDashboard.users.UsersController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import static util.Constants.*;

public class clientMainController implements Closeable, HttpStatusUpdate {
    private GridPane loginComponent;
    private LoginController logicController;
    @FXML private UsersController usersPanelController;
    @FXML private HeaderController headerPanelController;
    private final StringProperty currentUserName;

    public clientMainController() {
        currentUserName = new SimpleStringProperty(JHON_DOE);
    }

    @FXML
    public void initialize() {
        usersPanelController.setMainController(this);
        usersPanelController.startUsersAutoRefresh();
        headerPanelController.setMainController(this);

    }

    public void updateUserName(String userName) {
        currentUserName.set(userName);
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
        Platform.runLater(() -> {
            currentUserName.set(JHON_DOE);
            //chatRoomComponentController.setInActive();
            //setMainPanelTo(loginComponent);
        });
    }

    public void onLoginSuccess(String userName) {
        updateUserName(userName);              // updates header, etc.
        if (usersPanelController != null) {
            usersPanelController.startUsersAutoRefresh();  // begins polling /userslist
        }
    }


}
