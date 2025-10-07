// api/dto/ProgramSummary.java
package api.dto;

public class ProgramSummary {
    public String id;
    public String name;
    public String uploader;
    public int instructionCount;
    public int maxDegree;
    public int executions;
    public double avgCreditCost;

    public ProgramSummary() {}
    public ProgramSummary(String id, String name, String uploader,
                          int instructionCount, int maxDegree,
                          int executions, double avgCreditCost) {
        this.id = id; this.name = name; this.uploader = uploader;
        this.instructionCount = instructionCount; this.maxDegree = maxDegree;
        this.executions = executions; this.avgCreditCost = avgCreditCost;
    }
}
