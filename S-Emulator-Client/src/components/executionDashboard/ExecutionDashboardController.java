package components.executionDashboard;

import api.dto.*;

import components.executionDashboard.executionPanel.ExecutionPanelController;
import components.executionDashboard.header.HeaderController;
import components.executionDashboard.historyPanel.HistoryPanelController;
import components.executionDashboard.instructionPanel.InstructionTableController;
import components.shared.UserSession;

import javafx.application.Platform;
import javafx.fxml.FXML;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static util.Constants.GSON_INSTANCE;

/**
 * Phase-1 (server-first) controller:
 * - No local engine code.
 * - Pulls a full ExecutionStateDTO from the server whenever something changes.
 * - Pushes that state to child panels.
 */
public class ExecutionDashboardController {

    // Child controllers are injected automatically because their fx:ids
    // are the same as these field names + "Controller" suffix in the FXML.
    @FXML
    private HeaderController headerComponentController;
    @FXML
    private InstructionTableController instructionTableComponentController;
    @FXML
    private ExecutionPanelController executionPanelComponentController;
    @FXML
    private HistoryPanelController historyPanelComponentController;

    // Server-side session "context" (optional: the server can rely on JSESSIONID only)
    private String selectedFunction = null;
    private final UserSession userSession;

    public ExecutionDashboardController() {
        userSession = UserSession.getInstance();
    }

    @FXML
    public void initialize() {
        // Give children a back-reference if they expect it:
        if (headerComponentController != null) headerComponentController.setMainController(this);
        if (instructionTableComponentController != null) instructionTableComponentController.setMainController(this);
        if (executionPanelComponentController != null) executionPanelComponentController.setMainController(this);
        if (historyPanelComponentController != null) historyPanelComponentController.setMainController(this);
    }

    /**
     * Get the execution panel controller so other components can configure it before opening
     */
    public ExecutionPanelController getExecutionPanelController() {
        return executionPanelComponentController;
    }

    /* ---------------------------
       Public API called by UI
       --------------------------- */

    /**
     * Called when we first open the execution dashboard for a program/function the user chose.
     */
    public void openOnServer(String programIdOrFunctionName) {
        openOnServer(programIdOrFunctionName, null, 0);
    }

    /**
     * Open execution dashboard with pre-filled inputs (for Re-Run functionality)
     */
    public void openOnServer(String programIdOrFunctionName, List<VariableDTO> preFilledInputs, int targetDegree) {
        HttpUrl url = HttpUrl.parse(Constants.EXEC_OPEN)
                .newBuilder()
                .addQueryParameter("target", programIdOrFunctionName)
                .build();

        HttpClientUtil.runAsync(url.toString(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("HTTP Request failed: " + e.getMessage());
                Platform.runLater(() -> pushError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String json = response.body() != null ? response.body().string() : "";
                System.out.println("Received response: " + response.code() + " - " + json);

                if (!response.isSuccessful()) {
                    Platform.runLater(() -> pushError(shorten(json)));
                    return;
                }

                try {
                    ExecutionStateDTO state = GSON_INSTANCE.fromJson(json, ExecutionStateDTO.class);
                    System.out.println("Parsed ExecutionStateDTO - Instructions: " +
                            (state.getInstructions() != null ? state.getInstructions().size() : "null") +
                            ", Variables: " + (state.getAllVariables() != null ? state.getAllVariables().size() : "null"));

                    Platform.runLater(() -> {
                        applyStateToPanels(state);

                        // If we have pre-filled inputs, apply them after the state is loaded
                        if (preFilledInputs != null && !preFilledInputs.isEmpty()) {
                            applyPreFilledInputs(preFilledInputs);
                        }

                        // If target degree is specified and different from current, set it
                        if (targetDegree > 0 && targetDegree != state.getCurrentDegree()) {
                            setCurrentDegree(programIdOrFunctionName, targetDegree);
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Error parsing JSON response: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Run whole program/function (no client-side compute).
     */
    public void executeProgram() {
        executeProgram(Architecture.GENERATION_I); // Default to cheapest architecture
    }

    /**
     * Run whole program/function with specific architecture.
     */
    public void executeProgram(Architecture architecture) {
        System.out.println("ExecutionDashboardController: executeProgram called with architecture: " + architecture.name());

        // Create ExecuteProgramRequest with architecture
        ExecuteProgramRequest request = new ExecuteProgramRequest(selectedFunction, architecture);
        String requestJson = GSON_INSTANCE.toJson(request);

        System.out.println("Sending execute request: " + requestJson);

        // Make POST request to execution endpoint with architecture information
        HttpUrl url = HttpUrl.parse(Constants.EXEC_EXECUTE).newBuilder().build();

        HttpClientUtil.runAsyncPost(url.toString(), requestJson, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Execute program request failed: " + e.getMessage());
                Platform.runLater(() -> pushError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String json = response.body() != null ? response.body().string() : "";
                System.out.println("Execute program response: " + response.code() + " - " + json);

                if (!response.isSuccessful()) {
                    // Try to parse as ExecuteProgramResponse to get validation error details
                    try {
                        ExecuteProgramResponse errorResponse = GSON_INSTANCE.fromJson(json, ExecuteProgramResponse.class);
                        if (errorResponse != null && errorResponse.validationError != null && !errorResponse.validationError.isValid()) {
                            // Architecture validation failed - show detailed error
                            ArchitectureValidationDTO validation = errorResponse.validationError;
                            System.err.println("Architecture validation failed: " + validation.getErrorMessage());
                            System.err.println("Incompatible instruction IDs: " + validation.getIncompatibleInstructionIds());
                            System.err.println("Required architecture: " + validation.getRequiredArchitecture());
                            System.err.println("Provided architecture: " + validation.getProvidedArchitecture());

                            Platform.runLater(() -> {
                                // Show error message to user
                                showArchitectureValidationError(validation);

                                // Highlight incompatible instructions in red
                                if (instructionTableComponentController != null && validation.getIncompatibleInstructionIds() != null) {
                                    instructionTableComponentController.highlightIncompatibleInstructions(
                                        validation.getIncompatibleInstructionIds()
                                    );
                                }
                            });
                            return;
                        }
                    } catch (Exception parseError) {
                        // If parsing fails, just show generic error
                        System.err.println("Could not parse validation error: " + parseError.getMessage());
                    }

                    Platform.runLater(() -> pushError("Execution failed: " + shorten(json)));
                    return;
                }

                try {
                    // Check if response contains validation error even on success (shouldn't happen, but be safe)
                    ExecuteProgramResponse execResponse = GSON_INSTANCE.fromJson(json, ExecuteProgramResponse.class);
                    if (execResponse != null && execResponse.validationError != null && !execResponse.validationError.isValid()) {
                        // Validation failed
                        ArchitectureValidationDTO validation = execResponse.validationError;
                        Platform.runLater(() -> {
                            showArchitectureValidationError(validation);
                            if (instructionTableComponentController != null && validation.getIncompatibleInstructionIds() != null) {
                                instructionTableComponentController.highlightIncompatibleInstructions(
                                    validation.getIncompatibleInstructionIds()
                                );
                            }
                        });
                        return;
                    }

                    // Try to parse as ExecutionStateDTO for successful execution
                    ExecutionStateDTO state = GSON_INSTANCE.fromJson(json, ExecutionStateDTO.class);
                    System.out.println("Program executed successfully with architecture: " + architecture.name());
                    Platform.runLater(() -> applyStateToPanels(state));
                } catch (Exception e) {
                    System.err.println("Error parsing execution response: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> pushError("Error processing execution results"));
                }
            }
        });
    }

    /**
     * Degree change (+1) – expands.
     */
    public void expandProgram(String functionName) {
        setCurrentDegreeDelta(functionName, +1);
    }

    /**
     * Degree change (-1) – collapses.
     */
    public void collapseProgram(String functionName) {
        setCurrentDegreeDelta(functionName, -1);
    }

    /**
     * Direct set degree (spinner/slider).
     */
    public void setCurrentDegree(String functionName, int value) {
        HttpUrl url = HttpUrl.parse(Constants.EXEC_SET_DEGREE)
                .newBuilder()
                .addQueryParameter("name", functionName)
                .addQueryParameter("value", String.valueOf(value))
                .build();
        callAndApply(url);
    }

    public void onFunctionSelectionChanged(String name) {
        selectedFunction = name;
        HttpUrl url = HttpUrl.parse(Constants.EXEC_SELECT_FN)
                .newBuilder()
                .addQueryParameter("name", name)
                .build();
        callAndApply(url);
    }

    public void newRunButtonPressed() {
        callAndApply(HttpUrl.parse(Constants.EXEC_NEW_RUN).newBuilder().build());
    }

    /**
     * Set the program or function name in the header
     */
    public void setProgramOrFunctionName(String name) {
        if (headerComponentController != null) {
            headerComponentController.setProgramOrFunctionName(name);
        }
    }

    /* Debugging – entirely on server */
    public void startDebugging() {
        debugOp("start");
    }

    public void stepOver() {
        debugOp("step");
    }

    public void continueDebugging() {
        debugOp("cont");
    }

    public void stopDebugging() {
        debugOp("stop");
    }

    /* Optional read-only helpers that the header might bind to */
    public String getCurrentDegree() { /* value comes from latest state; header can store it */
        return "";
    }

    public String getMaxDegree() {
        return "";
    }

    /* ---------------------------------
       Internal: HTTP + apply state
       --------------------------------- */

    private void setCurrentDegreeDelta(String functionName, int delta) {
        HttpUrl url = HttpUrl.parse(Constants.EXEC_SET_DEGREE)
                .newBuilder()
                .addQueryParameter("name", functionName)
                .addQueryParameter("delta", String.valueOf(delta))
                .build();
        callAndApply(url);
    }

    private void debugOp(String op) {
        HttpUrl url = HttpUrl.parse(Constants.EXEC_DEBUG)
                .newBuilder()
                .addQueryParameter("op", op)
                .build();
        callAndApply(url);
    }

    private void callAndApply(HttpUrl url) {
        System.out.println("Making request to: " + url.toString());
        HttpClientUtil.runAsync(url.toString(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("HTTP Request failed: " + e.getMessage());
                Platform.runLater(() -> pushError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String json = response.body() != null ? response.body().string() : "";
                System.out.println("Received response: " + response.code() + " - " + json);

                if (!response.isSuccessful()) {
                    Platform.runLater(() -> pushError(shorten(json)));
                    return;
                }

                try {
                    // Check if response is valid JSON object before parsing
                    if (json.trim().isEmpty()) {
                        System.err.println("Empty response received from server");
                        Platform.runLater(() -> pushError("Empty response from server"));
                        return;
                    }

                    // Check if it's a plain string (starts with quotes but not a JSON object)
                    String trimmedJson = json.trim();
                    if (trimmedJson.startsWith("\"") && !trimmedJson.startsWith("{")) {
                        // It's a plain string - could be success or error message
                        String message = GSON_INSTANCE.fromJson(json, String.class);

                        // Check if it's a success message or error message
                        if (message.toLowerCase().contains("success") ||
                            message.toLowerCase().contains("updated")) {
                            // It's a success message - log it but don't show as error
                            System.out.println("Server success message: " + message);
                            // Don't update UI for success messages from intermediate operations
                            return;
                        } else {
                            // It's an error message
                            System.err.println("Server returned error message: " + message);
                            Platform.runLater(() -> pushError(message));
                            return;
                        }
                    }

                    ExecutionStateDTO state = GSON_INSTANCE.fromJson(json, ExecutionStateDTO.class);
                    System.out.println("Parsed ExecutionStateDTO - Instructions: " +
                            (state.getInstructions() != null ? state.getInstructions().size() : "null") +
                            ", Variables: " + (state.getAllVariables() != null ? state.getAllVariables().size() : "null"));
                    Platform.runLater(() -> applyStateToPanels(state));
                } catch (com.google.gson.JsonSyntaxException e) {
                    System.err.println("Invalid JSON format - server may have returned an error message");
                    System.err.println("Response was: " + json);
                    Platform.runLater(() -> pushError("Invalid response from server: " + shorten(json)));
                } catch (Exception e) {
                    System.err.println("Error parsing JSON response: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> pushError("Error processing response: " + e.getMessage()));
                }
            }
        });
    }

    /* ---------------------------------
       UI wiring: push DTO to panels
       --------------------------------- */

    private void applyStateToPanels(ExecutionStateDTO s) {
        if (s == null) return;

        // 1) Header (function selector, degree labels, etc.)
        if (headerComponentController != null) {
            List<String> names = s.getFunctionNames();
            headerComponentController.updateFunctionSelector(names);
            headerComponentController.setSelectedFunction(s.getSelectedFunction());
            headerComponentController.setProgramOrFunctionName(s.getSelectedFunction());
            headerComponentController.setDegreeLabels(s.getCurrentDegree(), s.getMaxDegree());
        }

        // 2) Instructions table
        if (instructionTableComponentController != null) {
            List<InstructionDTO> instructions = s.getInstructions();
            if (instructions != null && !instructions.isEmpty()) {
                System.out.println("Setting " + instructions.size() + " instructions to instruction table");
                instructionTableComponentController.setInstructions(instructions, s.getHighlightedInstructionId());
            } else {
                System.out.println("No instructions received from server");
            }
        } else {
            System.out.println("InstructionTableController is null - cannot set instructions");
        }

        // 3) Variables (all + inputs) + changed set + cycles
        if (executionPanelComponentController != null) {
            List<VariableDTO> allVars = s.getAllVariables();
            List<VariableDTO> inputVars = s.getInputVariables();
            Set<String> changed = s.getChangedVariableNames();
            if (allVars != null && !allVars.isEmpty()) {
                System.out.println("Setting " + allVars.size() + " variables to execution panel");
                executionPanelComponentController.setVariables(allVars, inputVars, changed);
            } else {
                System.out.println("No variables received from server");
            }
            executionPanelComponentController.setCyclesLabel(s.getCycles());
            executionPanelComponentController.updateDebugButtons(s.isDebugging());
        } else {
            System.out.println("ExecutionPanelController is null - cannot set variables");
        }

        // 4) History text/trace (server can render a list of strings or nodes)
        if (historyPanelComponentController != null) {
            historyPanelComponentController.setTraceLines(s.getTraceLines());
        }

    }

    private void pushError(String msg) {
        // You can route this to a status line if you have one
        System.err.println("[EXEC] " + msg);
    }

    /**
     * Show architecture validation error dialog to user
     */
    private void showArchitectureValidationError(ArchitectureValidationDTO validation) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Architecture Validation Error");
        alert.setHeaderText("Insufficient Architecture Selected");

        StringBuilder content = new StringBuilder();
        content.append(validation.getErrorMessage()).append("\n\n");
        content.append("Required: ").append(validation.getRequiredArchitecture().getDisplayName())
               .append(" (").append(validation.getRequiredArchitecture().getCost()).append(" credits)\n");
        content.append("Selected: ").append(validation.getProvidedArchitecture().getDisplayName())
               .append(" (").append(validation.getProvidedArchitecture().getCost()).append(" credits)\n\n");

        if (validation.getIncompatibleInstructionIds() != null && !validation.getIncompatibleInstructionIds().isEmpty()) {
            content.append("Incompatible instructions have been highlighted in red.\n");
            content.append("Please select ").append(validation.getRequiredArchitecture().getDisplayName())
                   .append(" or higher to execute this program.");
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private static String shorten(String s) {
        if (s == null) return "";
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() > 400 ? t.substring(0, 400) + " …" : t;
    }

    /**
     * Update input variable value on the server
     */
    public void updateInputValue(String variableName, int value) {
        System.out.println("ExecutionDashboardController: updateInputValue called - " + variableName + " = " + value);

        HttpUrl url = HttpUrl.parse(Constants.EXEC_UPDATE_INPUT)
                .newBuilder()
                .addQueryParameter("name", variableName)
                .addQueryParameter("value", String.valueOf(value))
                .build();

        System.out.println("Sending input update request: " + url.toString());
        callAndApply(url);
    }

    /**
     * Fetch parent command chain for a selected command
     */
    public void fetchParentCommandChain(int commandId) {
        HttpUrl url = HttpUrl.parse(Constants.EXEC_PARENT_CHAIN)
                .newBuilder()
                .addQueryParameter("commandId", String.valueOf(commandId))
                .build();

        System.out.println("Fetching parent chain for command ID: " + commandId);
        HttpClientUtil.runAsync(url.toString(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Failed to fetch parent chain: " + e.getMessage());
                Platform.runLater(() -> {
                    if (historyPanelComponentController != null) {
                        historyPanelComponentController.setHistoryChain(List.of());
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String json = response.body() != null ? response.body().string() : "";
                System.out.println("Parent chain response: " + json);

                if (!response.isSuccessful()) {
                    Platform.runLater(() -> {
                        if (historyPanelComponentController != null) {
                            historyPanelComponentController.setHistoryChain(List.of());
                        }
                    });
                    return;
                }

                try {
                    // Parse the JSON array of HistoryChainDTO objects
                    HistoryChainDTO[] parentChainArray = GSON_INSTANCE.fromJson(json, HistoryChainDTO[].class);
                    List<HistoryChainDTO> parentChain = Arrays.asList(parentChainArray);

                    Platform.runLater(() -> {
                        if (historyPanelComponentController != null) {
                            if (parentChain.isEmpty()) {
                                System.out.println("No parent command chain found for command ID: " + commandId);
                                historyPanelComponentController.setHistoryChain(List.of());
                            } else {
                                System.out.println("Displaying " + parentChain.size() + " history chain entries");
                                historyPanelComponentController.setHistoryChain(parentChain);
                            }
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Error parsing parent chain JSON: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        if (historyPanelComponentController != null) {
                            historyPanelComponentController.setHistoryChain(List.of());
                        }
                    });
                }
            }
        });
    }

    /**
     * Apply pre-filled input values (used during Re-Run)
     */
    private void applyPreFilledInputs(List<VariableDTO> preFilledInputs) {
        System.out.println("ExecutionDashboardController: Applying " + preFilledInputs.size() + " pre-filled inputs");

        for (VariableDTO input : preFilledInputs) {
            System.out.println("  Setting input: " + input.getName() + " = " + input.getValue());
            updateInputValue(input.getName(), input.getValue());
        }

        // Notify the execution panel to update UI with these values
        if (executionPanelComponentController != null) {
            executionPanelComponentController.updateInputDisplayValues(preFilledInputs);
        }
    }

    /**
     * Validate architecture compatibility by delegating to instruction table controller
     * Returns the required architecture if incompatible, null if compatible
     */
    public Architecture validateArchitectureCompatibility(Architecture selectedArchitecture) {
        if (instructionTableComponentController != null) {
            return instructionTableComponentController.validateArchitectureCompatibility(selectedArchitecture);
        }
        return null;
    }
}
