package api.dto;

public class FunctionInfoDTO {
    private String functionName;
    private String associatedProgram;
    private String associatedUser;
    private int numOfInstructions;
    private int maxDegree;

    // Constructor
    public FunctionInfoDTO(String functionName, String associatedProgram, String associatedUser,
                          int numOfInstructions, int maxDegree) {
        this.functionName = functionName;
        this.associatedProgram = associatedProgram;
        this.associatedUser = associatedUser;
        this.numOfInstructions = numOfInstructions;
        this.maxDegree = maxDegree;
    }

    // Default constructor
    public FunctionInfoDTO() {}

    // Getters and setters
    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getAssociatedProgram() { return associatedProgram; }
    public void setAssociatedProgram(String associatedProgram) { this.associatedProgram = associatedProgram; }

    public String getAssociatedUser() { return associatedUser; }
    public void setAssociatedUser(String associatedUser) { this.associatedUser = associatedUser; }

    public int getNumOfInstructions() { return numOfInstructions; }
    public void setNumOfInstructions(int numOfInstructions) { this.numOfInstructions = numOfInstructions; }

    public int getMaxDegree() { return maxDegree; }
    public void setMaxDegree(int maxDegree) { this.maxDegree = maxDegree; }
}
