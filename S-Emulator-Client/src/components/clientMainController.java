package components;

import components.api.HttpStatusUpdate;
import components.login.LoginController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import static util.Constants.*;

public class clientMainController implements Closeable, HttpStatusUpdate {
    private GridPane loginComponent;
    private LoginController logicController;
    @FXML
    private AnchorPane mainPanel;
    private final StringProperty currentUserName;

    public clientMainController() {
        currentUserName = new SimpleStringProperty(JHON_DOE);
    }

    @FXML
    public void initialize() {
    }

    public void updateUserName(String userName) {
        currentUserName.set(userName);
    }

    private void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }

    @Override
    public void close() throws IOException {
        //chatRoomComponentController.close();
    }

    private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            logicController = fxmlLoader.getController();
            logicController.setClientMainController(this);
            setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatRoomPage() {
//        URL loginPageUrl = getClass().getResource(CHAT_ROOM_FXML_RESOURCE_LOCATION);
//        try {
//            FXMLLoader fxmlLoader = new FXMLLoader();
//            fxmlLoader.setLocation(loginPageUrl);
//            chatRoomComponent = fxmlLoader.load();
//            chatRoomComponentController = fxmlLoader.getController();
//            chatRoomComponentController.setChatAppMainController(this);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void updateHttpLine(String line) {
        //httpStatusComponentController.addHttpStatusLine(line);
    }


    public void switchToLogin() {
        Platform.runLater(() -> {
            currentUserName.set(JHON_DOE);
            //chatRoomComponentController.setInActive();
            setMainPanelTo(loginComponent);
        });
    }
}
