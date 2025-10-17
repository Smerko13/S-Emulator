package components.mainDashboard.users;

import components.mainDashboard.clientMainController;
import api.dto.ExecutionHistoryDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import api.dto.UserSummary;



import static util.Constants.GSON_INSTANCE;
import static util.Constants.REFRESH_RATE;

public class UsersController {

    @FXML private Button showStatusButton;
    @FXML private Button reRunButton;
    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, String>  userNameColumn;
    @FXML private TableColumn<UserRow, Number>  programCountColumn;
    @FXML private TableColumn<UserRow, Number>  FunctionCountColumn;
    @FXML private TableColumn<UserRow, Number>  creditsColumn;
    @FXML private TableColumn<UserRow, Number>  creditsUsedColumn;
    @FXML private TableColumn<UserRow, Number>  executionsColumn;

    // Execution history table components
    @FXML private TableView<ExecutionHistoryRow> statsTable;
    @FXML private TableColumn<ExecutionHistoryRow, Number> executionNumberColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> runTypeColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> nameColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> architectureColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> expansionLevelColumn;
    @FXML private TableColumn<ExecutionHistoryRow, Number> cyclesColumn;
    @FXML private TableColumn<ExecutionHistoryRow, Number> outputColumn;

    private final ObservableList<UserRow> rows = FXCollections.observableArrayList();
    private final ObservableList<ExecutionHistoryRow> historyRows = FXCollections.observableArrayList();
    private clientMainController mainController;
    private String selectedUserId = null;

    private Timer timer;

    public void setMainController(clientMainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        System.out.println("UsersController: Initializing users table and execution history table");

        // Users table wiring
        userNameColumn.setCellValueFactory(c -> c.getValue().userNameProperty());
        programCountColumn.setCellValueFactory(c -> c.getValue().programsUploadedProperty());
        FunctionCountColumn.setCellValueFactory(c -> c.getValue().functionsUploadedProperty());
        creditsColumn.setCellValueFactory(c -> c.getValue().creditsAvailableProperty());
        creditsUsedColumn.setCellValueFactory(c -> c.getValue().creditsUsedProperty());
        executionsColumn.setCellValueFactory(c -> c.getValue().totalExecutionsProperty());

        usersTable.setItems(rows);

        // Execution history table wiring
        executionNumberColumn.setCellValueFactory(c -> c.getValue().runIdProperty());
        runTypeColumn.setCellValueFactory(c -> c.getValue().executionTypeProperty());
        nameColumn.setCellValueFactory(c -> c.getValue().programFunctionNameProperty());
        architectureColumn.setCellValueFactory(c -> c.getValue().architectureTypeProperty());
        expansionLevelColumn.setCellValueFactory(c -> c.getValue().executionLevelProperty());
        cyclesColumn.setCellValueFactory(c -> c.getValue().cpuCyclesUsedProperty());
        outputColumn.setCellValueFactory(c -> c.getValue().finalYValueProperty());

        statsTable.setItems(historyRows);

        // Initially disable buttons until an execution is selected
        showStatusButton.setDisable(true);
        reRunButton.setDisable(true);

        // Add selection listener to execution history table
        statsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            showStatusButton.setDisable(!hasSelection);
            reRunButton.setDisable(!hasSelection);

            if (newSelection != null) {
                System.out.println("UsersController: Execution selected - runId: " + newSelection.runIdProperty().get());
            } else {
                System.out.println("UsersController: Execution deselected");
            }
        });

        // Add selection listener to users table
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String userName = newSelection.userNameProperty().get();
                System.out.println("UsersController: User selected: " + userName);
                selectedUserId = userName;
                loadExecutionHistory(userName);
            }
        });

        // Load current user's history initially
        loadCurrentUserHistory();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        if (timer != null) return;
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loadUsersList();
            }
        }, 0, REFRESH_RATE); // Refresh every REFRESH_RATE ms
    }

    private void loadUsersList() {
        System.out.println("UsersController: loadUsersList() called");
        HttpClientUtil.runAsync(Constants.USERS_LIST, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("UsersController: loadUsersList failed: " + e.getMessage());
                if (mainController != null) mainController.updateHttpLine("userslist failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                System.out.println("UsersController: loadUsersList response: " + body);
                UserSummary[] summaries;
                try {
                    summaries = GSON_INSTANCE.fromJson(body, UserSummary[].class);
                    System.out.println("UsersController: Parsed " + summaries.length + " users");
                    for (UserSummary summary : summaries) {
                        System.out.println("  - " + summary.username + ": programs=" + summary.programs +
                                         ", functions=" + summary.functions + ", credits=" + summary.creditsAvailable +
                                         ", used=" + summary.creditsUsed + ", executions=" + summary.executions);
                    }
                } catch (Exception e) {
                    System.out.println("UsersController: Error parsing JSON: " + e.getMessage());
                    summaries = new UserSummary[0];
                }
                UserSummary[] finalSummaries = summaries;
                Platform.runLater(() -> {
                    System.out.println("UsersController: Updating table with " + finalSummaries.length + " users");
                    rows.setAll(Arrays.stream(finalSummaries).map(dto -> {
                        UserRow r = new UserRow(dto.username);
                        r.programsUploadedProperty().set(dto.programs);
                        r.functionsUploadedProperty().set(dto.functions);
                        r.creditsAvailableProperty().set(dto.creditsAvailable);
                        r.creditsUsedProperty().set(dto.creditsUsed);
                        r.totalExecutionsProperty().set(dto.executions);
                        return r;
                    }).toList());
                    System.out.println("UsersController: Table updated, now has " + rows.size() + " rows");
                });
            }
        });
    }

    public void cleanup() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Load execution history for a specific user
     */
    private void loadExecutionHistory(String userName) {
        System.out.println("UsersController: Loading execution history for user: " + userName);

        String url = Constants.EXECUTION_HISTORY + "?userId=" + userName;
        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Failed to load execution history for " + userName + ": " + e.getMessage());
                Platform.runLater(() -> {
                    historyRows.clear();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                System.out.println("UsersController: Received execution history response: " + body);

                if (!response.isSuccessful()) {
                    System.err.println("Server error loading execution history: " + response.code());
                    Platform.runLater(() -> historyRows.clear());
                    return;
                }

                ExecutionHistoryDTO[] historyArray;
                try {
                    historyArray = GSON_INSTANCE.fromJson(body, ExecutionHistoryDTO[].class);
                } catch (Exception e) {
                    System.err.println("Error parsing execution history JSON: " + e.getMessage());
                    historyArray = new ExecutionHistoryDTO[0];
                }

                ExecutionHistoryDTO[] finalArray = historyArray;
                Platform.runLater(() -> {
                    historyRows.clear();
                    for (ExecutionHistoryDTO dto : finalArray) {
                        ExecutionHistoryRow row = new ExecutionHistoryRow(
                            dto.runId,
                            dto.executionType,
                            dto.programFunctionName,
                            dto.architectureType,
                            dto.executionLevel,
                            dto.cpuCyclesUsed,
                            dto.finalYValue
                        );
                        historyRows.add(row);
                    }
                    System.out.println("UsersController: Loaded " + historyRows.size() + " execution records for " + userName);
                });
            }
        });
    }

    /**
     * Load execution history for the current logged-in user
     */
    private void loadCurrentUserHistory() {
        System.out.println("UsersController: Loading current user's execution history");

        HttpClientUtil.runAsync(Constants.EXECUTION_HISTORY, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Failed to load current user execution history: " + e.getMessage());
                Platform.runLater(() -> {
                    historyRows.clear();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                System.out.println("UsersController: Received current user history response: " + body);

                if (!response.isSuccessful()) {
                    System.err.println("Server error loading current user history: " + response.code());
                    Platform.runLater(() -> historyRows.clear());
                    return;
                }

                ExecutionHistoryDTO[] historyArray;
                try {
                    historyArray = GSON_INSTANCE.fromJson(body, ExecutionHistoryDTO[].class);
                } catch (Exception e) {
                    System.err.println("Error parsing current user history JSON: " + e.getMessage());
                    historyArray = new ExecutionHistoryDTO[0];
                }

                ExecutionHistoryDTO[] finalArray = historyArray;
                Platform.runLater(() -> {
                    historyRows.clear();
                    for (ExecutionHistoryDTO dto : finalArray) {
                        ExecutionHistoryRow row = new ExecutionHistoryRow(
                            dto.runId,
                            dto.executionType,
                            dto.programFunctionName,
                            dto.architectureType,
                            dto.executionLevel,
                            dto.cpuCyclesUsed,
                            dto.finalYValue
                        );
                        historyRows.add(row);
                    }
                    System.out.println("UsersController: Loaded " + historyRows.size() + " execution records for current user");
                });
            }
        });
    }

    /**
     * Handle unselect user button - revert to showing current user's history
     */
    public void unselectedUserPressed(javafx.event.ActionEvent e) {
        System.out.println("UsersController: Unselect user pressed - reverting to current user's history");

        // Clear user table selection
        usersTable.getSelectionModel().clearSelection();
        selectedUserId = null;

        // Load current user's execution history
        loadCurrentUserHistory();
    }

    // Button handlers for future implementation
    public void reRunButtonPressed(javafx.event.ActionEvent e) {
        System.out.println("UsersController: Re-Run button pressed (to be implemented)");
        // TODO: Implement re-run functionality
    }

    public void showStatusButtonPressed(javafx.event.ActionEvent e) {
        System.out.println("UsersController: Show Status button pressed (to be implemented)");
        // TODO: Implement show status functionality
    }

    public void startUsersAutoRefresh() {
        startAutoRefresh();
    }

    public void stopUsersAutoRefresh() {
        cleanup();
    }

    public void refreshUsersList() {
        loadUsersList();
    }
}
