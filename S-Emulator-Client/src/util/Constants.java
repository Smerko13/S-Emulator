package util;

import com.google.gson.Gson;

public class Constants {

    // global constants
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String JHON_DOE = "<Anonymous>";
    public final static int REFRESH_RATE = 2000;
    public final static String CHAT_LINE_FORMATTING = "%tH:%tM:%tS | %.10s: %s%n";

    // fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/components/mainDashboard/mainDashboard.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/components/login/login.fxml";
    public final static String CHAT_ROOM_FXML_RESOURCE_LOCATION = "/chat/client/component/chatroom/chat-room-main.fxml";

    // Server resources locations
    public static final int PORT = 8080;
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":" + PORT;
    public final static String CONTEXT_PATH = "/S_Emulator_Server";
    public final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;
    public static final String LOGIN_ENDPOINT = "login";
    public static final String VALIDATION_ENDPOINT = "validate";


    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/" + LOGIN_ENDPOINT;
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
    public final static String LOGOUT = FULL_SERVER_PATH + "/logout";
    //public final static String SEND_CHAT_LINE = FULL_SERVER_PATH + "/pages/chatroom/sendChat";
    //public final static String CHAT_LINES_LIST = FULL_SERVER_PATH + "/chat";

    // GSON instance
    public final static Gson GSON_INSTANCE = new Gson();

    public static final String EXEC_BASE        = FULL_SERVER_PATH + "/exec";
    public static final String EXEC_OPEN        = EXEC_BASE + "/open";           // ?programId=... or ?functionName=...
    public static final String EXEC_EXECUTE     = EXEC_BASE + "/execute";
    public static final String EXEC_SELECT_FN   = EXEC_BASE + "/selectFunction"; // ?name=...
    public static final String EXEC_SET_DEGREE  = EXEC_BASE + "/degree";         // ?value=...
    public static final String EXEC_DEBUG       = EXEC_BASE + "/debug";          // ?op=start|step|cont|stop
    public static final String EXEC_NEW_RUN     = EXEC_BASE + "/newRun";
    public static final String EXEC_STATE       = EXEC_BASE + "/state";
    public static final String EXEC_PARENT_CHAIN = EXEC_BASE + "/parentChain";   // ?commandId=...
}
