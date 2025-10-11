package api.dto;

import engine.commands.Command;

import java.util.List;

public class ProgramInfoDTO {
    private String programName;
    private String uploaderName;
    private int numOfInstructions;
    private int maxDegree;
    private int numOfExecutions;
    private double avgCreditCost;
    private List<Command> commands;

    // Constructor
    public ProgramInfoDTO(String programName, String uploaderName, int numOfInstructions,
                         int maxDegree, int numOfExecutions, double avgCreditCost) {
        this.programName = programName;
        this.uploaderName = uploaderName;
        this.numOfInstructions = numOfInstructions;
        this.maxDegree = maxDegree;
        this.numOfExecutions = numOfExecutions;
        this.avgCreditCost = avgCreditCost;
    }

    // Default constructor for JSON serialization
    public ProgramInfoDTO() {}

    // Getters and setters
    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public int getNumOfInstructions() { return numOfInstructions; }
    public void setNumOfInstructions(int numOfInstructions) { this.numOfInstructions = numOfInstructions; }

    public int getMaxDegree() { return maxDegree; }
    public void setMaxDegree(int maxDegree) { this.maxDegree = maxDegree; }

    public int getNumOfExecutions() { return numOfExecutions; }
    public void setNumOfExecutions(int numOfExecutions) { this.numOfExecutions = numOfExecutions; }

    public double getAvgCreditCost() { return avgCreditCost; }
    public void setAvgCreditCost(double avgCreditCost) { this.avgCreditCost = avgCreditCost; }

    public List<Command> getCommands() { return commands; }
    public void setCommands(List<Command> commands) { this.commands = commands; }
}
