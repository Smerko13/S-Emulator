package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.util.*;
import java.util.stream.Collectors;

public class JumpEqualFunction extends SyntheticCommand {
    private String JEFunctionLabel;
    private String functionName;

    // Keep raw arg text and a parsed list
    private String functionArgsRaw;
    private List<String> functionArguments;

    public JumpEqualFunction(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "JUMP_EQUAL_FUNCTION";

        // Target label
        JEFunctionLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.associatedLabels.add(JEFunctionLabel);
        this.associatedEngine.labels.add(JEFunctionLabel);
        this.isJumpCommand = true;

        // Function name resolution (program name -> user string)
        this.functionName = findCorrectFunctionName(
                instruction.getSInstructionArguments().getSInstructionArgument().get(1).getValue()
        );

        // Robust parse of argument string (supports nested calls like (Minus,x1,x2))
        String args = instruction.getSInstructionArguments().getSInstructionArgument().get(2).getValue();
        this.functionArgsRaw = (args == null) ? "" : args;
        this.functionArguments = initializeArgumentList(this.functionArgsRaw);

        // Costing info from callee
        this.cycles = 6 + calculateSubFunctionCycles();
        this.levelOfExpansion = calculateSubFunctionExpansionLevel() + 1;
    }

    private int calculateSubFunctionExpansionLevel() {
        for (Engine e : this.associatedEngine.subFunctions) {
            String userString = e.getUserString();
            if (userString.equals(functionName)) {
                return e.getMaxExpansionDepth();
            }
        }
        return 0;
    }

    private int calculateSubFunctionCycles() {
        for (Engine e : this.associatedEngine.subFunctions) {
            String userString = e.getUserString();
            if (userString.equals(functionName)) {
                return e.getTotalCycles();
            }
        }
        return 0;
    }

    @Override
    public void initializeExpandedCommands() {
        if (this.didInitialize) {
            this.getExpandedCommands().clear();
            expansionLogic();
            return;
        }
        expansionLogic();
        this.didInitialize = true;
    }

    private void expansionLogic() {
        // Scratch var to hold the function result so we can compare against this.variable
        WorkVariable newWorkVariable = new WorkVariable(generateNewWorkVariableName());
        this.associatedEngine.getVariables().add(newWorkVariable); // ensure visibility to emitted commands

        // Use the parsed arguments gathered in the ctor
        List<String> parsedArgs = new ArrayList<>(this.functionArguments);

        // Emit a Quote to compute the function into newWorkVariable.
        // Pass this.label so Quote can anchor properly (it will emit a Neutral if needed).
        this.ExpandedCommands.add(
                new Quote(newWorkVariable, this.functionName, parsedArgs, this.label, this, this.associatedEngine)
        );

        // Then jump if equal (compare this.variable to the computed value)
        this.ExpandedCommands.add(
                new JumpEqualVariable(this.variable, newWorkVariable, this.JEFunctionLabel, this, this.associatedEngine)
        );

        // Clean scratch (prevents later passes from seeing stale value)
        this.ExpandedCommands.add(
                new ConstantAssignment(newWorkVariable, 0, "   ", this, this.associatedEngine)
        );

        expandFurther();
    }

    @Override
    public String execute() {
        int returnValue = -1;

        // Snapshot existing variables' values so we can restore them after speculative eval
        Set<Variable> snapshot = this.associatedEngine.getVariables().stream()
                .map(Variable::clone)
                .collect(Collectors.toSet());

        // Track temporary variables created while resolving constants/nested calls
        List<Variable> execTemps = new ArrayList<>();

        try {
            // Build call var list from already-parsed tokens
            List<String> args = this.functionArguments;
            List<Variable> callVars = new ArrayList<>(args.size());
            for (String a : args) {
                callVars.add(resolveArgToVariable(a, execTemps));
            }

            for (Engine e : this.associatedEngine.subFunctions) {
                if (e.getUserString().equals(functionName)) {
                    returnValue = e.executeFunction(callVars, functionName, this.associatedEngine);
                    break;
                }
            }
        } finally {
            // Always restore values
            setBackOriginalVariables(snapshot);
            // Remove ephemeral temps created during evaluation
            if (!execTemps.isEmpty()) {
                this.associatedEngine.getVariables().removeAll(execTemps);
            }
        }

        return (this.variable.getValue() == returnValue) ? JEFunctionLabel : null;
    }

    /** Convert an argument token into a Variable.
     *  Handles: bare vars (x1/z2), constants like (CONST-3)/(C0), and nested calls like (Minus,x1,x2).
     *  Any temporary variables created are added to execTemps for cleanup.
     */
    private Variable resolveArgToVariable(String token, List<Variable> execTemps) {
        token = token.trim();

        // Case 1: bare variable name
        if (!token.isEmpty() && token.charAt(0) != '(') {
            for (Variable v : this.associatedEngine.getVariables()) {
                if (v.getName().equals(token)) return v;
            }
            throw new IllegalArgumentException("Unknown variable: " + token);
        }

        // Case 2: constants: (CONST-3), (CONST3), (C0), (C1), (C-1)
        if (token.startsWith("(CONST") || token.equals("(C0)") || token.equals("(C1)") || token.equals("(C-1)")) {
            int val = parseConstValue(token);
            WorkVariable tmp = new WorkVariable(generateNewWorkVariableName());
            tmp.setValue(val);
            this.associatedEngine.getVariables().add(tmp);
            execTemps.add(tmp);
            return tmp;
        }

        // Case 3: nested call e.g. (Minus,x1,x2)
        // Strip parens, split once on first comma for function vs rest
        String inner = token.substring(1, token.length() - 1);
        int comma = inner.indexOf(',');
        String nestedFunc = (comma == -1) ? inner : inner.substring(0, comma);
        String nestedArgs = (comma == -1) ? "" : inner.substring(comma + 1);

        List<String> nestedList = initializeArgumentList(nestedArgs);
        List<Variable> nestedVars = new ArrayList<>(nestedList.size());
        for (String a : nestedList) nestedVars.add(resolveArgToVariable(a, execTemps));

        int result = -1;
        for (Engine e : this.associatedEngine.subFunctions) {
            if (e.getUserString().equals(nestedFunc)) {
                result = e.executeFunction(nestedVars, nestedFunc, this.associatedEngine);
                break;
            }
        }
        WorkVariable tmp = new WorkVariable(generateNewWorkVariableName());
        tmp.setValue(result);
        this.associatedEngine.getVariables().add(tmp);
        execTemps.add(tmp);
        return tmp;
    }

    private int parseConstValue(String token) {
        // Examples: (CONST-3), (CONST3), (C0), (C1), (C-1)
        String t = token.replace("(", "").replace(")", "");
        if (t.equals("C0")) return 0;
        if (t.equals("C1")) return 1;
        if (t.equals("C-1")) return -1;
        if (t.startsWith("CONST")) {
            String n = t.substring("CONST".length());
            if (n.isEmpty()) return 0;
            return Integer.parseInt(n);
        }
        throw new IllegalArgumentException("Bad CONST token: " + token);
    }

    /** Split a comma-separated argument string into tokens, respecting nested parentheses.
     *  e.g. "x1,(Minus,x2,(CONST-3)),z4" -> ["x1", "(Minus,x2,(CONST-3))", "z4"]
     */
    private List<String> initializeArgumentList(String s) {
        List<String> out = new ArrayList<>();
        if (s == null) return out;
        s = s.trim();
        if (s.isEmpty()) return out;

        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                depth++;
                cur.append(c);
            } else if (c == ')') {
                depth = Math.max(0, depth - 1);
                cur.append(c);
            } else if (c == ',' && depth == 0) {
                String tok = cur.toString().trim();
                if (!tok.isEmpty()) out.add(tok);
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        String tok = cur.toString().trim();
        if (!tok.isEmpty()) out.add(tok);
        return out;
    }

    private void setBackOriginalVariables(Set<Variable> snapshot) {
        for (Variable v : snapshot) {
            for (Variable originalVar : this.associatedEngine.getVariables()) {
                if (v.getName().equals(originalVar.getName())) {
                    originalVar.setValue(v.getValue());
                }
            }
        }
    }

    @Override
    public boolean isValid() { // adjust if you have validation rules
        return true;
    }

    @Override
    public String getTargetLabel() {
        return this.JEFunctionLabel;
    }

    @Override
    public Set<Variable> getAllVariables() {
        return Collections.singleton(this.variable);
    }

    @Override
    public JumpEqualFunction clone() {
        return (JumpEqualFunction) super.clone();
    }

    @Override
    public Collection<String> getAssociatedLabels() {
        // NOTE: if your codebase forbids nulls in List.of, replace with a defensive list
        return List.of(this.label, this.JEFunctionLabel);
    }

    @Override
    public String toString() {
        if (functionArguments == null || functionArguments.isEmpty()) {
            return "IF " + this.variable.getName() + " = (" + functionName + ") GOTO " + JEFunctionLabel;
        } else if (functionArguments.size() == 1) {
            return "IF " + this.variable.getName() + " = (" + functionName + "," + functionArguments.getFirst() + ") GOTO " + JEFunctionLabel;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String arg : functionArguments) sb.append(arg).append(", ");
            sb.setLength(sb.length() - 2);
            return "IF " + this.variable.getName() + " = (" + functionName + "," + sb + ") GOTO " + JEFunctionLabel;
        }
    }

    private String findCorrectFunctionName(String functionName) {
        for (Engine e : this.associatedEngine.subFunctions) {
            if (e.getCurrentProgramName().equals(functionName)) {
                return e.getUserString();
            }
        }
        return null;
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {
        if (newLabel == null) return;

        if (this.label != null && this.label.equals(lbl)) {
            this.label = newLabel;
            this.associatedLabels.remove(lbl);
            this.associatedLabels.add(newLabel);
        }
        if (this.JEFunctionLabel != null && this.JEFunctionLabel.equals(lbl)) {
            this.JEFunctionLabel = newLabel;
            this.associatedLabels.remove(lbl);
            this.associatedLabels.add(newLabel);
        }
    }

    @Override
    public int getExpansionDepth() {
        int maxDepth = 1; // this command
        for (Command cmd : this.getExpandedCommands()) {
            if (cmd.getExpansionDepth() > maxDepth) {
                maxDepth = cmd.getExpansionDepth() + 1;
            }
        }
        return maxDepth;
    }
}
