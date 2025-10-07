// api/dto/StatsDTO.java
package api.dto;

import java.util.List;

public class StatsDTO {
    private List<Execution> executions;

    public StatsDTO() {}
    public StatsDTO(List<Execution> executions) { this.executions = executions; }

    public List<Execution> getExecutions() { return executions; }
    public void setExecutions(List<Execution> executions) { this.executions = executions; }

    /** One row in the stats table. */
    public static class Execution {
        private int executionNumber;
        private int expansionLevel;
        private int cycles;
        private String outputText;                // e.g. "y = 42"
        private List<VariableDTO> inputVars;      // inputs used for this run

        public Execution() {}
        public Execution(int executionNumber, int expansionLevel, int cycles,
                         String outputText, List<VariableDTO> inputVars) {
            this.executionNumber = executionNumber;
            this.expansionLevel  = expansionLevel;
            this.cycles          = cycles;
            this.outputText      = outputText;
            this.inputVars       = inputVars;
        }

        public int getExecutionNumber() { return executionNumber; }
        public int getExpansionLevel()  { return expansionLevel;  }
        public int getCycles()          { return cycles;          }
        public String getOutputText()   { return outputText;      }
        public List<VariableDTO> getInputVars() { return inputVars; }
    }
}
