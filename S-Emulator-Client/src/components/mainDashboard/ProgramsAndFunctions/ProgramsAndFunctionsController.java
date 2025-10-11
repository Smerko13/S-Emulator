package components.mainDashboard.ProgramsAndFunctions;

import components.executionDashboard.ExecutionDashboardController;
import components.mainDashboard.clientMainController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import api.dto.ProgramInfoDTO;
import api.dto.FunctionInfoDTO;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import static util.Constants.FULL_SERVER_PATH;

public class ProgramsAndFunctionsController implements Initializable {
    @FXML
    private TableColumn<ProgramInfoDTO, String> programNameColumn;
    @FXML
    private TableColumn<ProgramInfoDTO, String> uploaderNameColumn;
    @FXML
    private TableColumn<ProgramInfoDTO, Integer> numOfInstructionsColumn;
    @FXML
    private TableColumn<ProgramInfoDTO, Integer> maxDegreeColumn;
    @FXML
    private TableColumn<ProgramInfoDTO, Integer> numOfExecutionsColumn;
    @FXML
    private TableColumn<ProgramInfoDTO, Double> avgCreditCostColumn;
    @FXML
    private Button executeProgramButton;
    @FXML
    private TableColumn<FunctionInfoDTO, String> functionNameColumn;
    @FXML
    private TableColumn<FunctionInfoDTO, String> associatedProgramColumn;
    @FXML
    private TableColumn<FunctionInfoDTO, String> associatedUserColumn;
    @FXML
    private TableColumn<FunctionInfoDTO, Integer> numOfInstructionsInFunctionColumn;
    @FXML
    private TableColumn<FunctionInfoDTO, Integer> maxDegreeForFunctionColumn;
    @FXML
    private Button executeFunctionButton;
    @FXML
    public TableView<ProgramInfoDTO> programsTable;
    @FXML
    public TableView<FunctionInfoDTO> functionsTable;
    private ObservableList<ProgramInfoDTO> programsData = FXCollections.observableArrayList();
    private ObservableList<FunctionInfoDTO> functionsData = FXCollections.observableArrayList();
    private clientMainController clientMainController;
    private Timer refreshTimer;

    public void setMainController(clientMainController mainController) {
        this.clientMainController = mainController;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableColumns();
        loadProgramsAndFunctions();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loadProgramsAndFunctions();
            }
        }, 5000, 5000); // Refresh every 5 seconds
    }

    // Add cleanup method
    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }


    // ADD THIS MISSING METHOD
    private void initializeTableColumns() {
        // Programs table columns
        programNameColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        uploaderNameColumn.setCellValueFactory(new PropertyValueFactory<>("uploaderName"));
        numOfInstructionsColumn.setCellValueFactory(new PropertyValueFactory<>("numOfInstructions"));
        maxDegreeColumn.setCellValueFactory(new PropertyValueFactory<>("maxDegree"));
        numOfExecutionsColumn.setCellValueFactory(new PropertyValueFactory<>("numOfExecutions"));
        avgCreditCostColumn.setCellValueFactory(new PropertyValueFactory<>("avgCreditCost"));

        // Functions table columns
        functionNameColumn.setCellValueFactory(new PropertyValueFactory<>("functionName"));
        associatedProgramColumn.setCellValueFactory(new PropertyValueFactory<>("associatedProgram"));
        associatedUserColumn.setCellValueFactory(new PropertyValueFactory<>("associatedUser"));
        numOfInstructionsInFunctionColumn.setCellValueFactory(new PropertyValueFactory<>("numOfInstructions"));
        maxDegreeForFunctionColumn.setCellValueFactory(new PropertyValueFactory<>("maxDegree"));

        // Set data to tables
        programsTable.setItems(programsData);
        functionsTable.setItems(functionsData);

        // Initially disable execute buttons
        executeProgramButton.setDisable(true);
        executeFunctionButton.setDisable(true);

        // Add selection listeners to enable/disable buttons
        programsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            executeProgramButton.setDisable(newSelection == null);
            // Clear function table selection when program is selected
            if (newSelection != null) {
                functionsTable.getSelectionModel().clearSelection();
            }
        });

        functionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            executeFunctionButton.setDisable(newSelection == null);
            // Clear program table selection when function is selected
            if (newSelection != null) {
                programsTable.getSelectionModel().clearSelection();
            }
        });
    }

    // ADD THIS MISSING METHOD
    public void loadProgramsAndFunctions() {
        CompletableFuture.supplyAsync(this::fetchProgramsFromServer)
                .thenAccept(this::updateTablesOnUIThread)
                .exceptionally(throwable -> {
                    System.err.println("Error loading programs: " + throwable.getMessage());
                    return null;
                });
    }

    // ADD THIS MISSING METHOD
    private String fetchProgramsFromServer() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FULL_SERVER_PATH + "/programs"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ADD THIS MISSING METHOD
    private void updateTablesOnUIThread(String jsonResponse) {
        Platform.runLater(() -> {
            if (jsonResponse != null) {
                parseAndUpdateTables(jsonResponse);
            }
        });
    }

    // ADD THIS MISSING METHOD
    private void parseAndUpdateTables(String jsonResponse) {
        System.out.println("Received JSON: " + jsonResponse);

        try {
            // Clear existing data
            programsData.clear();
            functionsData.clear();

            // Parse programs
            if (jsonResponse != null && jsonResponse.contains("\"programs\":[")) {
                // Extract programs from JSON manually (basic parsing)
                String programsSection = jsonResponse.substring(
                        jsonResponse.indexOf("\"programs\":[") + 12,
                        jsonResponse.indexOf("],\"functions\":[")
                );

                // If there are programs, parse them
                if (!programsSection.trim().isEmpty() && !programsSection.equals("")) {
                    // Split by program objects (basic approach)
                    String[] programs = programsSection.split("\\},\\{");

                    for (String program : programs) {
                        // Clean up the program string
                        program = program.replace("{", "").replace("}", "");

                        // Extract values using simple string parsing
                        String programName = extractJsonValue(program, "programName");
                        String uploaderName = extractJsonValue(program, "uploaderName");
                        int numInstructions = Integer.parseInt(extractJsonValue(program, "numOfInstructions"));
                        int maxDegree = Integer.parseInt(extractJsonValue(program, "maxDegree"));
                        int numExecutions = Integer.parseInt(extractJsonValue(program, "numOfExecutions"));
                        double avgCreditCost = Double.parseDouble(extractJsonValue(program, "avgCreditCost"));

                        // Create DTO and add to list
                        ProgramInfoDTO programInfo = new ProgramInfoDTO(
                                programName, uploaderName, numInstructions,
                                maxDegree, numExecutions, avgCreditCost
                        );
                        programsData.add(programInfo);
                    }
                }
            }

            // Parse functions
            if (jsonResponse != null && jsonResponse.contains("\"functions\":[")) {
                // Extract functions from JSON
                String functionsSection = jsonResponse.substring(
                        jsonResponse.indexOf("\"functions\":[") + 13,
                        jsonResponse.lastIndexOf("]}")
                );

                // If there are functions, parse them
                if (!functionsSection.trim().isEmpty() && !functionsSection.equals("")) {
                    // Split by function objects
                    String[] functions = functionsSection.split("\\},\\{");

                    for (String function : functions) {
                        // Clean up the function string
                        function = function.replace("{", "").replace("}", "");

                        // Extract values using simple string parsing
                        String functionName = extractJsonValue(function, "functionName");
                        String associatedProgram = extractJsonValue(function, "associatedProgram");
                        String associatedUser = extractJsonValue(function, "associatedUser");
                        int numInstructions = Integer.parseInt(extractJsonValue(function, "numOfInstructions"));
                        int maxDegree = Integer.parseInt(extractJsonValue(function, "maxDegree"));

                        // Create DTO and add to list
                        FunctionInfoDTO functionInfo = new FunctionInfoDTO(
                                functionName, associatedProgram, associatedUser,
                                numInstructions, maxDegree
                        );
                        functionsData.add(functionInfo);
                    }
                }
            }

            System.out.println("Parsed " + programsData.size() + " programs and " + functionsData.size() + " functions");

        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();

            // Add test data if parsing fails
            programsData.add(new ProgramInfoDTO("Test Program", "Test User", 10, 5, 2, 1.5));
            functionsData.add(new FunctionInfoDTO("Test Function", "Test Program", "Test User", 5, 3));
        }
    }

    private String extractJsonValue(String jsonString, String key) {
        try {
            String keyPattern = "\"" + key + "\":";
            int startIndex = jsonString.indexOf(keyPattern) + keyPattern.length();

            if (startIndex == keyPattern.length() - 1) {
                return "0"; // Key not found
            }

            // Skip whitespace and quotes
            while (startIndex < jsonString.length() &&
                    (jsonString.charAt(startIndex) == ' ' || jsonString.charAt(startIndex) == '"')) {
                startIndex++;
            }

            int endIndex = startIndex;

            // Find end of value (next comma, quote, or end of string)
            while (endIndex < jsonString.length() &&
                    jsonString.charAt(endIndex) != ',' &&
                    jsonString.charAt(endIndex) != '"' &&
                    jsonString.charAt(endIndex) != '}') {
                endIndex++;
            }

            return jsonString.substring(startIndex, endIndex).trim();
        } catch (Exception e) {
            return "0"; // Default value if extraction fails
        }
    }

    public void executeProgramButtonPressed(ActionEvent actionEvent) throws IOException {
        // Get the selected program
        ProgramInfoDTO selectedProgram = programsTable.getSelectionModel().getSelectedItem();
        if (selectedProgram == null) {
            return; // No program selected
        }

        // Load the execution dashboard FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/executionDashboard/executionDashboard.fxml"));
        Parent executionDashboardRoot = loader.load();

        // Get the controller and set the program name
        Object controller = loader.getController();
        if (controller instanceof ExecutionDashboardController) {
            ExecutionDashboardController dashboardController = (ExecutionDashboardController) controller;
            // Assuming the dashboard controller has access to the header controller
            dashboardController.setUp(selectedProgram);
            //dashboardController.setProgramOrFunctionName(selectedProgram.getProgramName());
            //dashboardController.setCurrentDegree(selectedProgram.getProgramName(),0);

        }

        // Get the current stage from the button (similar to your working pattern)
        Stage stage = (Stage) executeProgramButton.getScene().getWindow();

        // Apply the same transition pattern that works in login
        if (stage != null && executionDashboardRoot != null) {
            if (stage.getScene() == null) {
                stage.setScene(new Scene(executionDashboardRoot));
            } else {
                stage.getScene().setRoot(executionDashboardRoot);
            }
            stage.setMaximized(true);
            stage.centerOnScreen();
        }

        // Set the title for the execution dashboard
        stage.setTitle("S-Emulator - Execution Dashboard");
    }

    public void executeFunctionButtonPressed(ActionEvent e) throws IOException {
        // Get the selected function
        FunctionInfoDTO selectedFunction = functionsTable.getSelectionModel().getSelectedItem();
        if (selectedFunction == null) {
            return; // No function selected
        }

        // Load the execution dashboard FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/executionDashboard/executionDashboard.fxml"));
        Parent executionDashboardRoot = loader.load();

        // Get the controller and set the function name
        Object controller = loader.getController();
        if (controller instanceof ExecutionDashboardController) {
            ExecutionDashboardController dashboardController = (ExecutionDashboardController) controller;
            // Assuming the dashboard controller has access to the header controller
            dashboardController.setProgramOrFunctionName(selectedFunction.getFunctionName());
        }

        // Get the current stage from the button (similar to your working pattern)
        Stage stage = (Stage) executeFunctionButton.getScene().getWindow();

        // Apply the same transition pattern that works in login
        if (stage != null && executionDashboardRoot != null) {
            if (stage.getScene() == null) {
                stage.setScene(new Scene(executionDashboardRoot));
            } else {
                stage.getScene().setRoot(executionDashboardRoot);
            }
            stage.setMaximized(true);
            stage.centerOnScreen();
        }

        // Set the title for the execution dashboard
        stage.setTitle("S-Emulator - Function Execution Dashboard");
    }
}
