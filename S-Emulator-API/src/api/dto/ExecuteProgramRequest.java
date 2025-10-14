// api/dto/ExecuteProgramRequest.java
package api.dto;

public class ExecuteProgramRequest {
    public String programId;
    public Architecture architecture;

    public ExecuteProgramRequest() {}

    public ExecuteProgramRequest(String programId) {
        this.programId = programId;
        this.architecture = Architecture.GENERATION_I; // Default to cheapest
    }

    public ExecuteProgramRequest(String programId, Architecture architecture) {
        this.programId = programId;
        this.architecture = architecture;
    }
}
