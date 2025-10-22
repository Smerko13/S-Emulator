package components.mainDashboard.users;

import api.dto.ProgramInfoDTO;
import components.executionDashboard.ExecutionDashboardController;
import components.mainDashboard.clientMainController;
import api.dto.ExecutionHistoryDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
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
    @FXML private TableColumn<ExecutionHistoryRow, Number> expansionLevelColumn;
    @FXML private TableColumn<ExecutionHistoryRow, Number> cyclesColumn;
    @FXML private TableColumn<ExecutionHistoryRow, Number> outputColumn;

    private final ObservableList<UserRow> rows = FXCollections.observableArrayList();
    private final ObservableList<ExecutionHistoryRow> historyRows = FXCollections.observableArrayList();
    private clientMainController mainController;
    private String selectedUserId = null;

    private Timer timer;

    // Store selected identifiers to restore after refresh
    private String selectedUserName = null;
    private Integer selectedExecutionRunId = null;

    public void setMainController(clientMainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

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
        expansionLevelColumn.setCellValueFactory(c -> c.getValue().expansionDegreeProperty());  // Now shows degree number
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
        });

        // Add selection listener to users table
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String userName = newSelection.userNameProperty().get();
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

        // Store current selection and scroll position before refresh
        UserRow currentUserSelection = usersTable.getSelectionModel().getSelectedItem();
        if (currentUserSelection != null) {
            selectedUserName = currentUserSelection.userNameProperty().get();
        }

        HttpClientUtil.runAsync(Constants.USERS_LIST, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (mainController != null) mainController.updateHttpLine("userslist failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // Handle 304 Not Modified - no need to update UI
                if (response.code() == 304) {
                    return;
                }

                // Cache the ETag for next request
                String etag = response.header("ETag");
                if (etag != null) {
                    HttpClientUtil.cacheETag(Constants.USERS_LIST, etag);
                }

                String body = response.body() != null ? response.body().string() : "[]";
                UserSummary[] summaries;
                try {
                    summaries = GSON_INSTANCE.fromJson(body, UserSummary[].class);
                } catch (Exception e) {
                    summaries = new UserSummary[0];
                }
                UserSummary[] finalSummaries = summaries;
                Platform.runLater(() -> {

                    // Smart update: compare and update only changed rows to avoid flicker
                    updateUsersTableSmart(finalSummaries);


                    // Restore user selection after refresh
                    if (selectedUserName != null) {
                        for (int i = 0; i < rows.size(); i++) {
                            if (rows.get(i).userNameProperty().get().equals(selectedUserName)) {
                                usersTable.getSelectionModel().select(i);
                                break;
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Smart update that only modifies changed rows to prevent flicker
     */
    private void updateUsersTableSmart(UserSummary[] summaries) {
        // Create a map of existing rows by username
        java.util.Map<String, UserRow> existingRows = new java.util.HashMap<>();
        for (UserRow row : rows) {
            existingRows.put(row.userNameProperty().get(), row);
        }

        // Track which usernames are in the new data
        java.util.Set<String> newUsernames = new java.util.HashSet<>();

        for (UserSummary summary : summaries) {
            newUsernames.add(summary.username);
            UserRow existingRow = existingRows.get(summary.username);

            if (existingRow != null) {
                // Update existing row only if values changed
                if (existingRow.programsUploadedProperty().get() != summary.programs) {
                    existingRow.programsUploadedProperty().set(summary.programs);
                }
                if (existingRow.functionsUploadedProperty().get() != summary.functions) {
                    existingRow.functionsUploadedProperty().set(summary.functions);
                }
                if (existingRow.creditsAvailableProperty().get() != summary.creditsAvailable) {
                    existingRow.creditsAvailableProperty().set(summary.creditsAvailable);
                }
                if (existingRow.creditsUsedProperty().get() != summary.creditsUsed) {
                    existingRow.creditsUsedProperty().set(summary.creditsUsed);
                }
                if (existingRow.totalExecutionsProperty().get() != summary.executions) {
                    existingRow.totalExecutionsProperty().set(summary.executions);
                }
            } else {
                // Add new row
                UserRow newRow = new UserRow(summary.username);
                newRow.programsUploadedProperty().set(summary.programs);
                newRow.functionsUploadedProperty().set(summary.functions);
                newRow.creditsAvailableProperty().set(summary.creditsAvailable);
                newRow.creditsUsedProperty().set(summary.creditsUsed);
                newRow.totalExecutionsProperty().set(summary.executions);
                rows.add(newRow);
            }
        }

        // Remove rows that are no longer in the data
        rows.removeIf(row -> !newUsernames.contains(row.userNameProperty().get()));
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

        // Store current execution selection before refresh
        ExecutionHistoryRow currentExecSelection = statsTable.getSelectionModel().getSelectedItem();
        if (currentExecSelection != null) {
            selectedExecutionRunId = currentExecSelection.getRunId();
        }

        String url = Constants.EXECUTION_HISTORY + "?userId=" + userName;
        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    historyRows.clear();
                    selectedExecutionRunId = null;
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";

                if (!response.isSuccessful()) {
                    Platform.runLater(() -> {
                        historyRows.clear();
                        selectedExecutionRunId = null;
                    });
                    return;
                }

                ExecutionHistoryDTO[] historyArray;
                try {
                    historyArray = GSON_INSTANCE.fromJson(body, ExecutionHistoryDTO[].class);
                } catch (Exception e) {
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
                            dto.expansionDegree,  // Now uses the degree number from DTO
                            dto.cpuCyclesUsed,
                            dto.finalYValue
                        );
                        historyRows.add(row);
                    }

                    // Restore execution selection after refresh
                    if (selectedExecutionRunId != null) {
                        for (int i = 0; i < historyRows.size(); i++) {
                            if (historyRows.get(i).getRunId() == selectedExecutionRunId) {
                                statsTable.getSelectionModel().select(i);
                                break;
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Load execution history for the current logged-in user
     */
    private void loadCurrentUserHistory() {

        // Store current execution selection before refresh
        ExecutionHistoryRow currentExecSelection = statsTable.getSelectionModel().getSelectedItem();
        if (currentExecSelection != null) {
            selectedExecutionRunId = currentExecSelection.getRunId();
        }

        HttpClientUtil.runAsync(Constants.EXECUTION_HISTORY, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    historyRows.clear();
                    selectedExecutionRunId = null;
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";

                if (!response.isSuccessful()) {
                    Platform.runLater(() -> {
                        historyRows.clear();
                        selectedExecutionRunId = null;
                    });
                    return;
                }

                ExecutionHistoryDTO[] historyArray;
                try {
                    historyArray = GSON_INSTANCE.fromJson(body, ExecutionHistoryDTO[].class);
                } catch (Exception e) {
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
                            dto.expansionDegree,  // Now uses the degree number from DTO
                            dto.cpuCyclesUsed,
                            dto.finalYValue
                        );
                        historyRows.add(row);
                    }

                    // Restore execution selection after refresh
                    if (selectedExecutionRunId != null) {
                        for (int i = 0; i < historyRows.size(); i++) {
                            if (historyRows.get(i).getRunId() == selectedExecutionRunId) {
                                statsTable.getSelectionModel().select(i);
                                break;
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Handle unselect user button - revert to showing current user's history
     */
    public void unselectedUserPressed(javafx.event.ActionEvent e) {

        // Clear user table selection
        usersTable.getSelectionModel().clearSelection();
        selectedUserId = null;
        selectedUserName = null;
        selectedExecutionRunId = null;

        // Load current user's execution history
        loadCurrentUserHistory();
    }

    // Button handlers for future implementation
    public void reRunButtonPressed(javafx.event.ActionEvent e) {
        ExecutionHistoryRow selected = statsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        int runId = selected.getRunId();

        // Build URL with runId and optionally userId
        String url = Constants.EXECUTION_DETAILS + "?runId=" + runId;
        if (selectedUserId != null) {
            url += "&userId=" + selectedUserId;
        }

        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to Load Execution Details");
                    alert.setContentText("Could not retrieve execution details for re-run: " + ex.getMessage());
                    alert.showAndWait();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : null;

                if (!response.isSuccessful()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Server Error");
                        alert.setContentText("Server returned error code: " + response.code());
                        alert.showAndWait();
                    });
                    return;
                }

                try {
                    api.dto.ExecutionDetailsDTO details = GSON_INSTANCE.fromJson(body, api.dto.ExecutionDetailsDTO.class);
                    Platform.runLater(() -> {
                        openExecutionDashboardForReRun(details);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Parse Error");
                        alert.setContentText("Could not parse execution details: " + ex.getMessage());
                        alert.showAndWait();
                    });
                }
            }
        });
    }

    /**
     * Open the execution dashboard for re-running a previous execution
     */
    private void openExecutionDashboardForReRun(api.dto.ExecutionDetailsDTO details) {
        try {

            // Load the execution dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION.replace("mainDashboard", "executionDashboard")));
            javafx.scene.Parent executionDashboardRoot = loader.load();

            // Get the controller
            ExecutionDashboardController execController = loader.getController();

            // Get the current stage
            javafx.stage.Stage stage = (javafx.stage.Stage) statsTable.getScene().getWindow();

            // IMPORTANT: Store reference to main dashboard root before switching
            javafx.scene.Parent mainDashboardRoot = (javafx.scene.Parent) stage.getScene().getRoot();

            // Pass the main dashboard root to the execution panel so it can navigate back without losing state
            if (execController.getExecutionPanelController() != null) {
                execController.getExecutionPanelController().setMainDashboardRoot(mainDashboardRoot);
            } else {
            }

            // Switch to execution dashboard scene
            if (stage.getScene() == null) {
                stage.setScene(new javafx.scene.Scene(executionDashboardRoot));
            } else {
                stage.getScene().setRoot(executionDashboardRoot);
            }
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.setTitle("S-Emulator - Execution Dashboard (Re-Run)");

            // Use ORIGINAL INPUT VALUES from before execution (not final values after execution)
            java.util.List<api.dto.VariableDTO> inputVariables = new java.util.ArrayList<>();
            if (details.originalInputs != null && !details.originalInputs.isEmpty()) {
                // Use the stored original inputs
                inputVariables.addAll(details.originalInputs);
                for (api.dto.VariableDTO var : details.originalInputs) {
                }
            } else {
                // Fallback: extract inputs from finalVariables if originalInputs not available
                if (details.finalVariables != null) {
                    for (api.dto.VariableDTO var : details.finalVariables) {
                        if ("Input".equalsIgnoreCase(var.getType()) || var.isInput()) {
                            inputVariables.add(var);
                        }
                    }
                }
            }

            // Parse execution level to get the degree
            int targetDegree = 0;
            try {
                targetDegree = Integer.parseInt(details.executionLevel);
            } catch (NumberFormatException ex) {
                // Default to 0 if parsing fails
            }

            // Open the program with pre-filled ORIGINAL inputs and target degree
            execController.openOnServer(details.programFunctionName, inputVariables, targetDegree);


        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Open Execution Dashboard");
            alert.setContentText("Could not open execution dashboard: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    public void showStatusButtonPressed(javafx.event.ActionEvent e) {
        ExecutionHistoryRow selected = statsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        int runId = selected.getRunId();

        // Build URL with runId and optionally userId
        String url = Constants.EXECUTION_DETAILS + "?runId=" + runId;
        if (selectedUserId != null) {
            url += "&userId=" + selectedUserId;
        }

        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to Load Execution Details");
                    alert.setContentText("Could not retrieve execution details: " + ex.getMessage());
                    alert.showAndWait();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : null;

                if (!response.isSuccessful()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Server Error");
                        alert.setContentText("Server returned error code: " + response.code());
                        alert.showAndWait();
                    });
                    return;
                }

                try {
                    api.dto.ExecutionDetailsDTO details = GSON_INSTANCE.fromJson(body, api.dto.ExecutionDetailsDTO.class);
                    Platform.runLater(() -> {
                        // Get the main stage from the button's scene
                        Stage ownerStage = (Stage) ((Button) e.getSource()).getScene().getWindow();
                        ExecutionStatusDialog.show(details, ownerStage);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Parse Error");
                        alert.setContentText("Could not parse execution details: " + ex.getMessage());
                        alert.showAndWait();
                    });
                }
            }
        });
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
