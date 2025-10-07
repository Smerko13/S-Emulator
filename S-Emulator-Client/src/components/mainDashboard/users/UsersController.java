package components.mainDashboard.users;

import components.clientMainController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static util.Constants.GSON_INSTANCE;
import static util.Constants.REFRESH_RATE;

public class UsersController {

    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, String>  userNameColumn;
    @FXML private TableColumn<UserRow, Number>  programCountColumn;
    @FXML private TableColumn<UserRow, Number>  FunctionCountColumn;
    @FXML private TableColumn<UserRow, Number>  creditsColumn;
    @FXML private TableColumn<UserRow, Number>  creditsUsedColumn;
    @FXML private TableColumn<UserRow, Number>  executionsColumn;

    private final ObservableList<UserRow> rows = FXCollections.observableArrayList();
    private clientMainController mainController;

    private Timer timer;

    public void setMainController(clientMainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Table wiring
        userNameColumn.setCellValueFactory(c -> c.getValue().userNameProperty());
        programCountColumn.setCellValueFactory(c -> c.getValue().programsUploadedProperty());
        FunctionCountColumn.setCellValueFactory(c -> c.getValue().functionsUploadedProperty());
        creditsColumn.setCellValueFactory(c -> c.getValue().creditsAvailableProperty());
        creditsUsedColumn.setCellValueFactory(c -> c.getValue().creditsUsedProperty());
        executionsColumn.setCellValueFactory(c -> c.getValue().totalExecutionsProperty());

        usersTable.setItems(rows);
    }

    /** start polling /userslist every REFRESH_RATE ms */
    public void startUsersAutoRefresh() {
        if (timer != null) return;
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override public void run() {
                HttpClientUtil.runAsync(Constants.USERS_LIST, new Callback() {
                    @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        if (mainController != null) mainController.updateHttpLine("userslist failed: " + e.getMessage());
                    }
                    @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String body = response.body() != null ? response.body().string() : "[]";
                        String[] names = new String[0];
                        try { names = GSON_INSTANCE.fromJson(body, String[].class); }
                        catch (Exception ignore) { /* keep empty */ }

                        // map names -> rows (other fields zero for now)
                        String[] finalNames = names;
                        Platform.runLater(() -> {
                            rows.setAll(Arrays.stream(finalNames).map(UserRow::new).toList());
                        });
                    }
                });
            }
        }, 0, REFRESH_RATE);
    }

    /** stop polling (optional, call on app close) */
    public void stopUsersAutoRefresh() {
        if (timer != null) { timer.cancel(); timer = null; }
    }

    // Your button handlers can stay empty for now
    public void reRunButtonPressed(javafx.event.ActionEvent e) {}
    public void showStatusButtonPressed(javafx.event.ActionEvent e) {}
    public void unselectedUserPressed(javafx.event.ActionEvent e) {}
}
