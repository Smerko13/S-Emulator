import com.google.gson.Gson;

// Server configuration
public static final String FULL_SERVER_PATH = "http://localhost:8080/S_Emulator_Server_Web_exploded";

        // FXML Resource Locations
        public static final String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/components/mainDashboard/mainDashboard.fxml";
        public static final String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/components/login/loginPage.fxml";

        // Execution endpoints
        public static final String EXEC_OPEN = FULL_SERVER_PATH + "/exec/open";
        public static final String EXEC_EXECUTE = FULL_SERVER_PATH + "/exec/execute";
        public static final String EXEC_SET_DEGREE = FULL_SERVER_PATH + "/exec/setDegree";
        public static final String EXEC_SELECT_FN = FULL_SERVER_PATH + "/exec/selectFunction";
        public static final String EXEC_NEW_RUN = FULL_SERVER_PATH + "/exec/newRun";
        public static final String EXEC_DEBUG = FULL_SERVER_PATH + "/exec/debug";

        // Gson instance for JSON serialization/deserialization
        public static final Gson GSON_INSTANCE = new Gson();
