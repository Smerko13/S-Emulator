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
import ui.instructionTable.InstructionTableController;

import javax.swing.text.TabableView;
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
        fxmlLoader = new FXMLLoader();
        url = getClass().getResource("base/base.fxml");
        fxmlLoader.setLocation(url);
        AnchorPane root = fxmlLoader.load(url.openStream());

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("S-Emulator");
        primaryStage.show();
    }
}