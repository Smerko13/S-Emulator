package ui;

import engine.Engine;
import engine.S_Emulator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.base.BaseController;
import ui.header.HeaderController;

import java.net.URL;

public class Main extends Application {
    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader;
        URL url;
        S_Emulator s_emulator = new Engine();

        // load header component and controller from fxml
        fxmlLoader = new FXMLLoader();
        url = getClass().getResource("header/header.fxml");
        fxmlLoader.setLocation(url);
        VBox headerComponent = fxmlLoader.load(url.openStream());
        HeaderController headerController = fxmlLoader.getController();

        // load master app and controller from fxml
        fxmlLoader = new FXMLLoader();
        url = getClass().getResource("base/base.fxml");
        fxmlLoader.setLocation(url);
        AnchorPane root = fxmlLoader.load(url.openStream());
        BaseController appController = fxmlLoader.getController();

        // connect between controllers
        appController.setHeaderComponentController(headerController);

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("S-Emulator");
        primaryStage.show();
    }

}
