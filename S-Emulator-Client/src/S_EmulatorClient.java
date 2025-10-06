import components.clientMainController;
import components.login.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static util.Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION;
import static util.Constants.LOGIN_PAGE_FXML_RESOURCE_LOCATION;

public class S_EmulatorClient extends Application {

    private Stage primaryStage;
    private clientMainController clientAppMainController;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("S-Emulator Client");

        URL mainUrl = getClass().getResource(MAIN_PAGE_FXML_RESOURCE_LOCATION);
        Objects.requireNonNull(mainUrl, "mainDashboard.fxml not found");
        FXMLLoader mainLoader = new FXMLLoader(mainUrl);
        Parent mainRoot = mainLoader.load();
        clientAppMainController = mainLoader.getController();

        URL loginUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        Objects.requireNonNull(loginUrl, "loginPage.fxml not found");
        FXMLLoader loginLoader = new FXMLLoader(loginUrl);
        Parent loginRoot = loginLoader.load();
        LoginController loginController = loginLoader.getController();

        loginController.setClientMainController(clientAppMainController);
        loginController.setStageAndMainRoot(primaryStage, mainRoot);

        primaryStage.setScene(new Scene(loginRoot));
        primaryStage.show();
    }

    @Override
    public void stop() {
        try {
            util.http.HttpClientUtil.shutdown();
            if (clientAppMainController != null) {
                clientAppMainController.close();
            }
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        launch(args);
    }
}
