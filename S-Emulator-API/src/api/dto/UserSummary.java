package api.dto;

/** Simple DTO shared between server and client. No framework deps. */
public class UserSummary {
    public String username;
    public int programs;
    public int functions;
    public int creditsAvailable;
    public int creditsUsed;
    public int executions;

    // Gson/Jackson need a no-arg ctor
    public UserSummary() {}

    public UserSummary(String username,
                       int programs,
                       int functions,
                       int creditsAvailable,
                       int creditsUsed,
                       int executions) {
        this.username = username;
        this.programs = programs;
        this.functions = functions;
        this.creditsAvailable = creditsAvailable;
        this.creditsUsed = creditsUsed;
        this.executions = executions;
    }
}
