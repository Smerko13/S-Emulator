package components.executionDashboard;

import api.dto.ExecutionStateDTO;
import api.dto.InstructionDTO;
import api.dto.StatsDTO;
import api.dto.VariableDTO;

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
    @FXML private HeaderController headerComponentController;
    @FXML private InstructionTableController instructionTableComponentController;
    @FXML private ExecutionPanelController executionPanelComponentController;
    @FXML private HistoryPanelController historyPanelComponentController;

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

    /* ---------------------------
       Public API called by UI
       --------------------------- */

    /** Called when we first open the execution dashboard for a program/function the user chose. */
    public void openOnServer(String programIdOrFunctionName) {
        HttpUrl url = HttpUrl.parse(Constants.EXEC_OPEN)
                .newBuilder()
                .addQueryParameter("target", programIdOrFunctionName)
                .build();
        callAndApply(url);
    }

    /** Run whole program/function (no client-side compute). */
    public void executeProgram() {
        callAndApply(HttpUrl.parse(Constants.EXEC_EXECUTE).newBuilder().build());
    }

    /** Degree change (+1) – expands. */
    public void expandProgram(String functionName) {
        setCurrentDegreeDelta(functionName, +1);
    }

    /** Degree change (-1) – collapses. */
    public void collapseProgram(String functionName) {
        setCurrentDegreeDelta(functionName, -1);
    }

    /** Direct set degree (spinner/slider). */
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

    /* Debugging – entirely on server */
    public void startDebugging()   { debugOp("start"); }
    public void stepOver()         { debugOp("step");  }
    public void continueDebugging(){ debugOp("cont");  }
    public void stopDebugging()    { debugOp("stop");  }

    /* Optional read-only helpers that the header might bind to */
    public String getCurrentDegree() { /* value comes from latest state; header can store it */ return ""; }
    public String getMaxDegree()     { return ""; }

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
        HttpClientUtil.runAsync(url.toString(), new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> pushError("Network error: " + e.getMessage()));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String json = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    Platform.runLater(() -> pushError(shorten(json)));
                    return;
                }
                ExecutionStateDTO state = GSON_INSTANCE.fromJson(json, ExecutionStateDTO.class);
                Platform.runLater(() -> applyStateToPanels(state));
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
            headerComponentController.setDegreeLabels(s.getCurrentDegree(), s.getMaxDegree());
        }

        // 2) Instructions table
        if (instructionTableComponentController != null) {
            List<InstructionDTO> instructions = s.getInstructions();
            instructionTableComponentController.setInstructions(instructions, s.getHighlightedInstructionId());
        }

        // 3) Variables (all + inputs) + changed set + cycles
        if (executionPanelComponentController != null) {
            List<VariableDTO> allVars   = s.getAllVariables();
            List<VariableDTO> inputVars = s.getInputVariables();
            Set<String>       changed   = s.getChangedVariableNames();
            executionPanelComponentController.setVariables(allVars, inputVars, changed);
            executionPanelComponentController.setCyclesLabel(s.getCycles());
            executionPanelComponentController.updateDebugButtons(s.isDebugging());
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

    private static String shorten(String s) {
        if (s == null) return "";
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() > 400 ? t.substring(0, 400) + " …" : t;
    }

    public void updateInputValue(String name, int value) {
        // TODO: POST to server, then fetch fresh ExecutionStateDTO and call applyState(...)
    }

    /* ---------------------------------
       User data management methods for header
       --------------------------------- */

    public String getUserName() {
        return userSession.getUserName();
    }

    public int getUserCredits() {
        return userSession.getCredits();
    }

    public String getCurrentUserId() {
        return userSession.getUserName().replaceAll("\\s+", "_").toLowerCase();
    }

}
