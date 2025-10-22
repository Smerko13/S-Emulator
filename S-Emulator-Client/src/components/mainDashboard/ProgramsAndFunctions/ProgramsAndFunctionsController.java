package components.mainDashboard.ProgramsAndFunctions;

import components.executionDashboard.ExecutionDashboardController;
import components.mainDashboard.clientMainController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import java.util.Timer;
import java.util.TimerTask;

import static util.Constants.GSON_INSTANCE;
import static util.Constants.REFRESH_RATE;

public class ProgramsAndFunctionsController {

    // Programs Table
    @FXML private TableView<ProgramRow> programsTable;
    @FXML private TableColumn<ProgramRow, String> programNameColumn;
    @FXML private TableColumn<ProgramRow, String> programUploaderColumn;
    @FXML private TableColumn<ProgramRow, Number> programInstructionsColumn;
    @FXML private TableColumn<ProgramRow, Number> programMaxLevelColumn;
    @FXML private TableColumn<ProgramRow, Number> programExecutionsColumn;
    @FXML private TableColumn<ProgramRow, Number> programAvgCostColumn;
    @FXML private Button executeProgramButton;

    // Functions Table
    @FXML private TableView<FunctionRow> functionsTable;
    @FXML private TableColumn<FunctionRow, String> functionNameColumn;
    @FXML private TableColumn<FunctionRow, String> parentProgramColumn;
    @FXML private TableColumn<FunctionRow, String> functionUploaderColumn;
    @FXML private TableColumn<FunctionRow, Number> functionInstructionsColumn;
    @FXML private TableColumn<FunctionRow, Number> functionMaxLevelColumn;
    @FXML private Button executeFunctionButton;

    private final ObservableList<ProgramRow> programRows = FXCollections.observableArrayList();
    private final ObservableList<FunctionRow> functionRows = FXCollections.observableArrayList();
    private clientMainController mainController;
    private Timer timer;

    // Store selected identifiers to restore after refresh
    private String selectedProgramName = null;
    private String selectedFunctionName = null;

    public void setMainController(clientMainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

        // Programs table wiring
        programNameColumn.setCellValueFactory(c -> c.getValue().programNameProperty());
        programUploaderColumn.setCellValueFactory(c -> c.getValue().uploaderNameProperty());
        programInstructionsColumn.setCellValueFactory(c -> c.getValue().instructionCountProperty());
        programMaxLevelColumn.setCellValueFactory(c -> c.getValue().maxLevelProperty());
        programExecutionsColumn.setCellValueFactory(c -> c.getValue().executionsCountProperty());
        programAvgCostColumn.setCellValueFactory(c -> c.getValue().avgCreditCostProperty());
        programsTable.setItems(programRows);

        // Functions table wiring
        functionNameColumn.setCellValueFactory(c -> c.getValue().functionNameProperty());
        parentProgramColumn.setCellValueFactory(c -> c.getValue().parentProgramProperty());
        functionUploaderColumn.setCellValueFactory(c -> c.getValue().uploaderNameProperty());
        functionInstructionsColumn.setCellValueFactory(c -> c.getValue().instructionCountProperty());
        functionMaxLevelColumn.setCellValueFactory(c -> c.getValue().maxLevelProperty());
        functionsTable.setItems(functionRows);

        // Initially disable execute buttons
        executeProgramButton.setDisable(true);
        executeFunctionButton.setDisable(true);

        // Add selection listeners
        programsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            executeProgramButton.setDisable(newSelection == null);
        });

        functionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            executeFunctionButton.setDisable(newSelection == null);
        });

        // Start auto-refresh
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        if (timer != null) return;
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loadProgramsAndFunctions();
            }
        }, 0, REFRESH_RATE); // Refresh every 2 seconds
    }

    public void stopAutoRefresh() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void loadProgramsAndFunctions() {

        // Store current selections before refresh
        ProgramRow currentProgramSelection = programsTable.getSelectionModel().getSelectedItem();
        if (currentProgramSelection != null) {
            selectedProgramName = currentProgramSelection.programNameProperty().get();
        }

        FunctionRow currentFunctionSelection = functionsTable.getSelectionModel().getSelectedItem();
        if (currentFunctionSelection != null) {
            selectedFunctionName = currentFunctionSelection.functionNameProperty().get();
        }

        String url = Constants.FULL_SERVER_PATH + "/programs";
        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
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
                    HttpClientUtil.cacheETag(url, etag);
                }

                String body = response.body() != null ? response.body().string() : "{}";

                if (!response.isSuccessful()) {
                    return;
                }

                try {
                    ProgramsAndFunctionsResponse data = GSON_INSTANCE.fromJson(body, ProgramsAndFunctionsResponse.class);

                    Platform.runLater(() -> {
                        // Smart update for programs table
                        updateProgramsTableSmart(data.programs);

                        // Restore program selection after refresh
                        if (selectedProgramName != null) {
                            for (int i = 0; i < programRows.size(); i++) {
                                if (programRows.get(i).programNameProperty().get().equals(selectedProgramName)) {
                                    programsTable.getSelectionModel().select(i);
                                    break;
                                }
                            }
                        }

                        // Smart update for functions table
                        updateFunctionsTableSmart(data.functions);

                        // Restore function selection after refresh
                        if (selectedFunctionName != null) {
                            for (int i = 0; i < functionRows.size(); i++) {
                                if (functionRows.get(i).functionNameProperty().get().equals(selectedFunctionName)) {
                                    functionsTable.getSelectionModel().select(i);
                                    break;
                                }
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Smart update that only modifies changed rows to prevent flicker
     */
    private void updateProgramsTableSmart(ProgramData[] programs) {
        if (programs == null) {
            programRows.clear();
            return;
        }

        // Create a map of existing rows by program name
        java.util.Map<String, ProgramRow> existingRows = new java.util.HashMap<>();
        for (ProgramRow row : programRows) {
            existingRows.put(row.programNameProperty().get(), row);
        }

        // Track which programs are in the new data
        java.util.Set<String> newProgramNames = new java.util.HashSet<>();

        for (ProgramData prog : programs) {
            newProgramNames.add(prog.programName);
            ProgramRow existingRow = existingRows.get(prog.programName);

            if (existingRow != null) {
                // Update existing row only if values changed
                if (existingRow.instructionCountProperty().get() != prog.numOfInstructions) {
                    existingRow.instructionCountProperty().set(prog.numOfInstructions);
                }
                if (existingRow.maxLevelProperty().get() != prog.maxDegree) {
                    existingRow.maxLevelProperty().set(prog.maxDegree);
                }
                if (existingRow.executionsCountProperty().get() != prog.numOfExecutions) {
                    existingRow.executionsCountProperty().set(prog.numOfExecutions);
                }
                if (Math.abs(existingRow.avgCreditCostProperty().get() - prog.avgCreditCost) > 0.01) {
                    existingRow.avgCreditCostProperty().set(prog.avgCreditCost);
                }
                if (!existingRow.uploaderNameProperty().get().equals(prog.uploaderName)) {
                    existingRow.uploaderNameProperty().set(prog.uploaderName);
                }
            } else {
                // Add new row
                ProgramRow newRow = new ProgramRow(
                    prog.programName,
                    prog.uploaderName,
                    prog.numOfInstructions,
                    prog.maxDegree,
                    prog.numOfExecutions,
                    prog.avgCreditCost
                );
                programRows.add(newRow);
            }
        }

        // Remove rows that are no longer in the data
        programRows.removeIf(row -> !newProgramNames.contains(row.programNameProperty().get()));
    }

    /**
     * Smart update for functions table
     */
    private void updateFunctionsTableSmart(FunctionData[] functions) {
        if (functions == null) {
            functionRows.clear();
            return;
        }

        // Create a map of existing rows by function name
        java.util.Map<String, FunctionRow> existingRows = new java.util.HashMap<>();
        for (FunctionRow row : functionRows) {
            existingRows.put(row.functionNameProperty().get(), row);
        }

        // Track which functions are in the new data
        java.util.Set<String> newFunctionNames = new java.util.HashSet<>();

        for (FunctionData func : functions) {
            newFunctionNames.add(func.functionName);
            FunctionRow existingRow = existingRows.get(func.functionName);

            if (existingRow != null) {
                // Update existing row only if values changed
                if (existingRow.instructionCountProperty().get() != func.numOfInstructions) {
                    existingRow.instructionCountProperty().set(func.numOfInstructions);
                }
                if (existingRow.maxLevelProperty().get() != func.maxDegree) {
                    existingRow.maxLevelProperty().set(func.maxDegree);
                }
                if (!existingRow.parentProgramProperty().get().equals(func.associatedProgram)) {
                    existingRow.parentProgramProperty().set(func.associatedProgram);
                }
                if (!existingRow.uploaderNameProperty().get().equals(func.associatedUser)) {
                    existingRow.uploaderNameProperty().set(func.associatedUser);
                }
            } else {
                // Add new row
                FunctionRow newRow = new FunctionRow(
                    func.functionName,
                    func.associatedProgram,
                    func.associatedUser,
                    func.numOfInstructions,
                    func.maxDegree
                );
                functionRows.add(newRow);
            }
        }

        // Remove rows that are no longer in the data
        functionRows.removeIf(row -> !newFunctionNames.contains(row.functionNameProperty().get()));
    }

    @FXML
    public void executeProgramButtonPressed() {
        ProgramRow selected = programsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String programName = selected.programNameProperty().get();
        openExecutionDashboard(programName);
    }

    @FXML
    public void executeFunctionButtonPressed() {
        FunctionRow selected = functionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String functionName = selected.functionNameProperty().get();
        openExecutionDashboard(functionName);
    }

    private void openExecutionDashboard(String targetName) {
        try {
            // Load the execution dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/executionDashboard/executionDashboard.fxml"));
            Parent executionDashboardRoot = loader.load();

            // Get the controller
            ExecutionDashboardController execController = loader.getController();

            // Get the current stage
            Stage stage = (Stage) programsTable.getScene().getWindow();

            // IMPORTANT: Store reference to main dashboard root before switching
            Parent mainDashboardRoot = (Parent) stage.getScene().getRoot();

            // Pass the main dashboard root to the execution panel so it can navigate back without losing state
            if (execController.getExecutionPanelController() != null) {
                execController.getExecutionPanelController().setMainDashboardRoot(mainDashboardRoot);
            } else {
            }

            // Switch to execution dashboard scene
            if (stage.getScene() == null) {
                stage.setScene(new Scene(executionDashboardRoot));
            } else {
                stage.getScene().setRoot(executionDashboardRoot);
            }

            // Open the target program/function
            execController.openOnServer(targetName);

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Open Execution Dashboard");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Inner classes for JSON parsing
    private static class ProgramsAndFunctionsResponse {
        ProgramData[] programs;
        FunctionData[] functions;
    }

    private static class ProgramData {
        String programName;
        String uploaderName;
        int numOfInstructions;
        int maxDegree;
        int numOfExecutions;
        double avgCreditCost;
    }

    private static class FunctionData {
        String functionName;
        String associatedProgram;
        String associatedUser;
        int numOfInstructions;
        int maxDegree;
    }
}

