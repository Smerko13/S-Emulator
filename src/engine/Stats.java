package engine;

import com.sun.jdi.connect.Connector;
import engine.arguments.Varible;
import engine.arguments.types.InputVarible;
import engine.arguments.types.OutputVarible;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Stats {
    List<Execution> executionHistory;

    public void updateStatEntry(int expansionLevel, Set<Varible> varibles, int cycleSum) {
        Execution execution = new Execution();
        execution.expansionLevel = expansionLevel;
        for (Varible varible : varibles) {
            if (varible instanceof InputVarible) {
                execution.inputVariables.add(new InputVarible((InputVarible) varible));
            } else if (varible instanceof OutputVarible) {
                execution.outputVariable = new OutputVarible((OutputVarible) varible);
            }
        }
        execution.cycleCount = cycleSum;

        this.executionHistory.add(execution);
    }

    private class Execution {
        private static int id = 0;
        private int currentId;
        private int expansionLevel;
        private List<Varible> inputVariables;
        private int cycleCount;
        private OutputVarible outputVariable;

        public Execution() {
            this.inputVariables = new ArrayList<>();
            id++;
            this.currentId = id;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Execution #").append(currentId).append("\n");
            sb.append("    Expansion Level: ").append(expansionLevel).append("\n");
            sb.append("    Input Variables: ");
            for (Varible var : inputVariables) {
                sb.append(var.getName()).append(" = ").append(var.getValue()).append(", ");
            }
            if (outputVariable != null) {
                sb.append("Output Variable: ").append(outputVariable.getName())
                  .append(" = ").append(outputVariable.getValue()).append("\n");
            } else {
                sb.append("No Output Variable\n");
            }
            sb.append("    Cycle Count: ").append(cycleCount).append("\n");
            return sb.toString();
        }
    }

    public Stats() {
        this.executionHistory = new java.util.ArrayList<>();
    }

    public void reset() {
        this.executionHistory.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Execution execution : executionHistory) {
            sb.append(execution.toString()).append("\n");
        }
        return sb.toString().trim();
    }


}
