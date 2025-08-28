package engine;

import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Stats implements Serializable {
    List<Execution> executionHistory;

    public void updateStatEntry(int expansionLevel, Set<Variable> variables, Set<Variable> extraInputVariables, int cycleSum) {
        Execution execution = new Execution();
        execution.expansionLevel = expansionLevel;
        for (Variable variable : variables) {
            if (variable instanceof InputVariable) {
                execution.inputVariables.add(new InputVariable((InputVariable) variable));
            } else if (variable instanceof OutputVariable) {
                execution.outputVariable = new OutputVariable((OutputVariable) variable);
            }
        }
        for (Variable variable : extraInputVariables) {

            execution.inputVariables.add(new InputVariable((InputVariable) variable));

        }
        execution.cycleCount = cycleSum;

        this.executionHistory.add(execution);
    }

    private class Execution implements Serializable {
        private static int id = 0;
        private final int currentId;
        private int expansionLevel;
        private List<Variable> inputVariables;
        private int cycleCount;
        private OutputVariable outputVariable;

        public Execution() {
            this.inputVariables = new ArrayList<>();
            id++;
            this.currentId = id;
        }

        private static void resetId() {
            id = 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Execution #").append(currentId).append("\n");
            sb.append("    Expansion Level: ").append(expansionLevel).append("\n");
            sb.append("    Input Variables: ");
            for (Variable var : inputVariables) {
                sb.append(var.getName()).append(" = ").append(((InputVariable)var).getOriginalValue()).append(", ");
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
        Execution.resetId();
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
