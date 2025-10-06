package components.login;

import components.clientMainController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import util.Constants;
import util.http.HttpClientUtil;
import java.io.IOException;


public class LoginController {
    @FXML private TextField userNameTextField;
    @FXML private Button loginButton;
    @FXML private Label errorMessageLabel;
    private Stage stage;
    private Parent mainRoot;
    private clientMainController clientMainController;
    private final StringProperty errorMessageProperty = new SimpleStringProperty();

    @FXML
    public void initialize() {
        errorMessageLabel.textProperty().bind(errorMessageProperty);
    }

    public void setStageAndMainRoot(Stage stage, Parent mainRoot) {
        this.stage = stage;
        this.mainRoot = mainRoot;
    }


    @FXML
    private void loginButtonClicked(ActionEvent event) {
        String userName = userNameTextField.getText();
        if (userName.isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        String ctx = Constants.CONTEXT_PATH.replaceFirst("^/+","");


        HttpUrl finalUrl = new HttpUrl.Builder()
                .scheme("http")
                .host(Constants.BASE_DOMAIN)
                .port(Constants.PORT)
                .addPathSegment(ctx)
                .addPathSegment(Constants.LOGIN_ENDPOINT)
                .addQueryParameter("username", userName)
                .build();

        updateHttpStatusLine("New request is launched for: " + finalUrl);

        HttpClientUtil.runAsync(finalUrl.toString(), new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Something went wrong: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                boolean ok = response.code() >= 200 && response.code() < 300;
                String body = response.body() != null ? response.body().string() : "";
                Platform.runLater(() -> {
                    if (!ok) {
                        errorMessageProperty.set("Something went wrong: " + response.code() + " " + body);
                        return;
                    }

                    if (stage != null && mainRoot != null) {
                        if (stage.getScene() == null) {
                            stage.setScene(new Scene(mainRoot));
                        } else {
                            stage.getScene().setRoot(mainRoot);
                        }
                        stage.centerOnScreen();
                    }

                    stage.setMaximized(true);
                    stage.centerOnScreen();

                    if (clientMainController != null) {
                        clientMainController.updateUserName(userName.trim());
                    }
                });
            }
        });
    }

    public void userNameKeyTyped(KeyEvent keyEvent) {
        errorMessageProperty.set("");
    }

    public void quitButtonClicked(ActionEvent actionEvent) {
        Platform.exit();
    }

    private void updateHttpStatusLine(String data) {
        if (clientMainController != null) clientMainController.updateHttpLine(data);
        else System.out.println("[HTTP] " + data);
    }

    public void setClientMainController(clientMainController c) {
        this.clientMainController = c;
        HttpClientUtil.setCookieManagerLoggingFacility(line ->
                Platform.runLater(() -> updateHttpStatusLine(line)));
    }
}
