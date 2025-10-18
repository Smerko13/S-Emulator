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

        // Store current selection before refresh
        UserRow currentUserSelection = usersTable.getSelectionModel().getSelectedItem();
        if (currentUserSelection != null) {
            selectedUserName = currentUserSelection.userNameProperty().get();
        }

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

        // Store current execution selection before refresh
        ExecutionHistoryRow currentExecSelection = statsTable.getSelectionModel().getSelectedItem();
        if (currentExecSelection != null) {
            selectedExecutionRunId = currentExecSelection.getRunId();
        }

        String url = Constants.EXECUTION_HISTORY + "?userId=" + userName;
        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Failed to load execution history for " + userName + ": " + e.getMessage());
                Platform.runLater(() -> {
                    historyRows.clear();
                    selectedExecutionRunId = null;
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                System.out.println("UsersController: Received execution history response: " + body);

                if (!response.isSuccessful()) {
                    System.err.println("Server error loading execution history: " + response.code());
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
                            dto.expansionDegree,  // Now uses the degree number from DTO
                            dto.cpuCyclesUsed,
                            dto.finalYValue
                        );
                        historyRows.add(row);
                    }
                    System.out.println("UsersController: Loaded " + historyRows.size() + " execution records for " + userName);

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
        System.out.println("UsersController: Loading current user's execution history");

        // Store current execution selection before refresh
        ExecutionHistoryRow currentExecSelection = statsTable.getSelectionModel().getSelectedItem();
        if (currentExecSelection != null) {
            selectedExecutionRunId = currentExecSelection.getRunId();
        }

        HttpClientUtil.runAsync(Constants.EXECUTION_HISTORY, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Failed to load current user execution history: " + e.getMessage());
                Platform.runLater(() -> {
                    historyRows.clear();
                    selectedExecutionRunId = null;
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                System.out.println("UsersController: Received current user history response: " + body);

                if (!response.isSuccessful()) {
                    System.err.println("Server error loading current user history: " + response.code());
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
                            dto.expansionDegree,  // Now uses the degree number from DTO
                            dto.cpuCyclesUsed,
                            dto.finalYValue
                        );
                        historyRows.add(row);
                    }
                    System.out.println("UsersController: Loaded " + historyRows.size() + " execution records for current user");

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
        System.out.println("UsersController: Unselect user pressed - reverting to current user's history");

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
            System.out.println("UsersController: No execution selected for re-run");
            return;
        }

        int runId = selected.getRunId();
        System.out.println("UsersController: Re-running execution with runId: " + runId);

        // Build URL with runId and optionally userId
        String url = Constants.EXECUTION_DETAILS + "?runId=" + runId;
        if (selectedUserId != null) {
            url += "&userId=" + selectedUserId;
        }

        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException ex) {
                System.err.println("Failed to load execution details for re-run " + runId + ": " + ex.getMessage());
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
                    System.err.println("Server error loading execution details for re-run: " + response.code());
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
                    System.err.println("Error parsing execution details JSON for re-run: " + ex.getMessage());
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
            System.out.println("UsersController: Opening execution dashboard for re-run");
            System.out.println("  Program: " + details.programFunctionName);
            System.out.println("  Execution Level: " + details.executionLevel);
            System.out.println("  Original Inputs: " + (details.originalInputs != null ? details.originalInputs.size() : 0));

            // Load the execution dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION.replace("mainDashboard", "executionDashboard")));
            javafx.scene.Parent executionDashboardRoot = loader.load();

            // Get the controller
            ExecutionDashboardController execController = loader.getController();

            // Get the current stage
            javafx.stage.Stage stage = (javafx.stage.Stage) statsTable.getScene().getWindow();

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
                System.out.println("UsersController: Using " + details.originalInputs.size() + " original inputs from before execution:");
                for (api.dto.VariableDTO var : details.originalInputs) {
                    System.out.println("  Input variable for re-run: " + var.getName() + " = " + var.getValue());
                }
            } else {
                // Fallback: extract inputs from finalVariables if originalInputs not available
                System.out.println("UsersController: Warning - originalInputs not available, falling back to finalVariables");
                if (details.finalVariables != null) {
                    for (api.dto.VariableDTO var : details.finalVariables) {
                        if ("Input".equalsIgnoreCase(var.getType()) || var.isInput()) {
                            inputVariables.add(var);
                            System.out.println("  Input variable for re-run (from final): " + var.getName() + " = " + var.getValue());
                        }
                    }
                }
            }

            // Parse execution level to get the degree
            int targetDegree = 0;
            try {
                targetDegree = Integer.parseInt(details.executionLevel);
            } catch (NumberFormatException ex) {
                System.out.println("Could not parse execution level as degree: " + details.executionLevel);
                // Default to 0 if parsing fails
            }

            // Open the program with pre-filled ORIGINAL inputs and target degree
            execController.openOnServer(details.programFunctionName, inputVariables, targetDegree);

            System.out.println("UsersController: Execution dashboard opened successfully for re-run");

        } catch (Exception ex) {
            System.err.println("Error opening execution dashboard for re-run: " + ex.getMessage());
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
            System.out.println("UsersController: No execution selected");
            return;
        }

        int runId = selected.getRunId();
        System.out.println("UsersController: Fetching execution details for runId: " + runId);

        // Build URL with runId and optionally userId
        String url = Constants.EXECUTION_DETAILS + "?runId=" + runId;
        if (selectedUserId != null) {
            url += "&userId=" + selectedUserId;
        }

        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException ex) {
                System.err.println("Failed to load execution details for runId " + runId + ": " + ex.getMessage());
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
                    System.err.println("Server error loading execution details: " + response.code());
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
                    System.err.println("Error parsing execution details JSON: " + ex.getMessage());
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
