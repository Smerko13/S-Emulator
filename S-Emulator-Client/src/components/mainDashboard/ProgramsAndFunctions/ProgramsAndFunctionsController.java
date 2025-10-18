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

    public void setMainController(clientMainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        System.out.println("ProgramsAndFunctionsController: Initializing");

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
        System.out.println("ProgramsAndFunctionsController: Loading programs and functions");

        String url = Constants.FULL_SERVER_PATH + "/programs";
        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Failed to load programs and functions: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "{}";

                if (!response.isSuccessful()) {
                    System.err.println("Server error loading programs: " + response.code());
                    return;
                }

                try {
                    ProgramsAndFunctionsResponse data = GSON_INSTANCE.fromJson(body, ProgramsAndFunctionsResponse.class);

                    Platform.runLater(() -> {
                        // Update programs table
                        programRows.clear();
                        if (data.programs != null) {
                            for (ProgramData prog : data.programs) {
                                ProgramRow row = new ProgramRow(
                                    prog.programName,
                                    prog.uploaderName,
                                    prog.numOfInstructions,
                                    prog.maxDegree,
                                    prog.numOfExecutions,
                                    prog.avgCreditCost
                                );
                                programRows.add(row);
                            }
                        }
                        System.out.println("Loaded " + programRows.size() + " programs");

                        // Update functions table
                        functionRows.clear();
                        if (data.functions != null) {
                            for (FunctionData func : data.functions) {
                                FunctionRow row = new FunctionRow(
                                    func.functionName,
                                    func.associatedProgram,
                                    func.associatedUser,
                                    func.numOfInstructions,
                                    func.maxDegree
                                );
                                functionRows.add(row);
                            }
                        }
                        System.out.println("Loaded " + functionRows.size() + " functions");
                    });

                } catch (Exception e) {
                    System.err.println("Error parsing programs/functions JSON: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    public void executeProgramButtonPressed() {
        ProgramRow selected = programsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String programName = selected.programNameProperty().get();
        System.out.println("Executing program: " + programName);
        openExecutionDashboard(programName);
    }

    @FXML
    public void executeFunctionButtonPressed() {
        FunctionRow selected = functionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String functionName = selected.functionNameProperty().get();
        System.out.println("Executing function: " + functionName);
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

            // Switch to execution dashboard scene
            if (stage.getScene() == null) {
                stage.setScene(new Scene(executionDashboardRoot));
            } else {
                stage.getScene().setRoot(executionDashboardRoot);
            }

            // Open the target program/function
            execController.openOnServer(targetName);

        } catch (Exception e) {
            System.err.println("Error opening execution dashboard: " + e.getMessage());
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

