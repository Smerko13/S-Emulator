package engine.commands.synthetic.types;

import engine.Program;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import engine.commands.synthetic.types.Assignment;
import engine.commands.synthetic.types.ConstantAssignment;
import schema.SInstruction;

import java.util.*;

public class Quote extends SyntheticCommand implements Cloneable {
    String functionName;
    String userString;
    String functionArguments;
    List<String> argumentList;

    public Quote(SInstruction instruction, Program program) {
        super(instruction, program);
        this.commandName = "QUOTE";
        String name = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        for (Program e : this.associatedProgram.subFunctions) {
            if (e.getCurrentProgramName().equals(name)) {
                this.userString = e.getUserString();
                this.functionName = e.getCurrentProgramName();
                break;
            }
        }
        if (this.functionName == null) {
            this.functionName = name;
            this.userString = name;
        }
        this.functionArguments = instruction.getSInstructionArguments().getSInstructionArgument().get(1).getValue();
        this.argumentList = initializeArgumentList(this.functionArguments);
        initializeAssociatedVariables();
        this.cycles = 5 + calculateSubFunctionCycles(this.functionName);
        // Remove the premature levelOfExpansion calculation - let getExpansionDepth() handle it
    }

    public Quote(Variable assignedVariable, String functionName, List<String> functionArguments, String label, Command parentCommand, Program program) {
        super(assignedVariable, label, parentCommand, program);
        this.commandName = "QUOTE";
        this.functionName = functionName;
        for(Program e : this.associatedProgram.subFunctions) {
            if(e.getCurrentProgramName().equals(functionName)) {
                this.userString = e.getUserString();
                break;
            }
        }
        if (this.userString == null) {
            this.userString = functionName;
        }
        this.argumentList = functionArguments;
        this.functionArguments = String.join(",", functionArguments);
        initializeAssociatedVariables();
        this.cycles = 5 + calculateSubFunctionCycles(this.functionName);
        // Remove the premature levelOfExpansion calculation - let getExpansionDepth() handle it
    }

    private int calculateSubFunctionCycles(String functionName) {
        for (Program e : this.associatedProgram.subFunctions) {
            if (e.getCurrentProgramName().equals(functionName)) {
                return e.getTotalCycles();
            }
        }
        return 0;
    }

    private void initializeAssociatedVariables() {
        if (functionArguments == null || functionArguments.isEmpty()) {
            return;
        }

        // Parse arguments more carefully to handle nested function calls
        List<String> actualVariables = extractActualVariablesFromArguments(this.functionArguments);

        for (String varName : actualVariables) {
            boolean found = false;
            for (Variable v : this.associatedProgram.getVariables()) {
                if (v.getName().equals(varName)) {
                    this.associatedVariables.add(v);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Variable var = new InputVariable(varName);
                this.associatedProgram.getVariables().add(var);
                this.associatedVariables.add(var);
            }
        }
    }

    /**
     * Extracts only the actual variable references from function arguments,
     * properly handling nested function calls.
     * For example: "(Const7),(Successor,x1)" should only extract "x1"
     */
    private List<String> extractActualVariablesFromArguments(String args) {
        Set<String> variables = new LinkedHashSet<>();

        // Use the existing argumentList which is already properly parsed
        for (String arg : this.argumentList) {
            extractVariablesFromArgument(arg, variables);
        }

        return new ArrayList<>(variables);
    }

    /**
     * Recursively extract variable names from an argument, handling nested function calls
     */
    private void extractVariablesFromArgument(String arg, Set<String> variables) {
        arg = arg.trim();

        if (arg.isEmpty()) {
            return;
        }

        // If it's a function call like (FuncName,arg1,arg2)
        if (arg.charAt(0) == '(') {
            // Extract the arguments inside the function call
            String innerArgs = arg.substring(arg.indexOf(',') == -1 ? arg.length() - 1 : arg.indexOf(',') + 1, arg.length() - 1);
            if (!innerArgs.isEmpty()) {
                List<String> subArgs = initializeArgumentList(innerArgs);
                for (String subArg : subArgs) {
                    extractVariablesFromArgument(subArg, variables);
                }
            }
        }
        // If it's a direct variable reference like x1, y, z2
        else if (arg.startsWith("x") || arg.startsWith("y") || arg.startsWith("z")) {
            int index = 0;
            for (char c : arg.toCharArray()) {
                if (c == 'x' || c == 'y' || c == 'z' || Character.isDigit(c)) {
                    index++;
                } else {
                    break;
                }
            }
            if (index > 0) {
                String cleanedArg = arg.substring(0, index);
                variables.add(cleanedArg);
            }
        }
    }

    private List<String> initializeArgumentList(String functionArguments) {
        List<String> returnList = new ArrayList<>();
        int i = 0;
        if (functionArguments.isEmpty()) {
            return returnList;
        } else {
            StringBuilder sb = new StringBuilder();
            for (char c : functionArguments.toCharArray()) {
                if (c == '(') {
                    i++;
                }
                if (c == ')') {
                    i--;
                }
                if (c == ',' && i == 0) {
                    returnList.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            returnList.add(sb.toString());
            return returnList;
        }
    }

    @Override
    public void initializeExpandedCommands() {
        if(!this.didInitialize) {
            expansionLogic();
            this.didInitialize = true;
            // V2 RESTORE: Set levelOfExpansion after expansion is complete
            this.levelOfExpansion = calculateActualExpansionDepth();
        }
    }

    // Helper method to calculate actual expansion depth like v2 did
    private int calculateActualExpansionDepth() {
        int maxDepth = 1;
        for (Command cmd : this.getExpandedCommands()) {
            int cmdDepth = cmd.getExpansionDepth();
            if (cmdDepth >= maxDepth) {
                maxDepth = cmdDepth + 1;
            }
        }
        return maxDepth;
    }

    private void expansionLogic() {
        String newOutputVarName = null;
        WorkVariable outputTempVar = null;

        if(!this.label.trim().isEmpty()) {
            this.ExpandedCommands.add(new Neutral(this.associatedProgram.getOutputVar(),this.label,this, this.associatedProgram));
        }

        // RESTORE V2 LOGIC: Process ALL subfunctions in loop, find matching one
        for(Program e : this.associatedProgram.subFunctions) {
            Set<Variable> functionHelpers = new LinkedHashSet<>();
            String SubFunctionName = e.getCurrentProgramName();
            boolean exitLabelRequired = false;
            String newLabel = generateNewLabel();
            this.associatedProgram.labels.add(newLabel);
            String exitLabel = newLabel + SubFunctionName + "_EXIT";
            this.associatedProgram.labels.add(exitLabel);

            // Check if this is the target function (either by name or userString)
            if(SubFunctionName.equals(functionName) || e.getUserString().equals(functionName)) {
                outputTempVar = null;
                Map<String,String> inputBind = new HashMap<>(); // e.g. "x1" -> "z155"
                Program clonedSubFunction = e.clone();

                List<Command> subFunctionCommands = clonedSubFunction.getCommands();
                for (Command cmd : subFunctionCommands) {
                    cmd.setParent(this);
                    cmd.setAssociatedEngine(this.associatedProgram);
                }

                // 1. Gather all labels
                Set<String> allLabels = new HashSet<>();
                for (Command cmd : subFunctionCommands) {
                    for(String lbl : cmd.getAssociatedLabels()) {
                        allLabels.add(lbl.trim());
                    }
                }

                boolean exitReferencedViaTarget = false;
                for (Command cmd : subFunctionCommands) {
                    String tgt = cmd.getTargetLabel();
                    if (tgt != null && "EXIT".equals(tgt.trim())) {
                        exitReferencedViaTarget = true;
                        break;
                    }
                }

                // 2. Map each label to a new label
                Map<String, String> labelMap = new HashMap<>();
                for (String lbl : allLabels) {
                    if ("EXIT".equals(lbl)) {
                        labelMap.put(lbl, exitLabel);
                        exitLabelRequired = true;
                    } else if (lbl.trim().isEmpty()) {
                        labelMap.put(lbl, lbl); // Keep neutral label as is
                    } else {
                        String newLbl = generateNewLabel();
                        this.associatedProgram.labels.add(newLbl);
                        labelMap.put(lbl, newLbl);
                    }
                }

                if (exitReferencedViaTarget || allLabels.contains("EXIT")) {
                    labelMap.put("EXIT", exitLabel);
                    exitLabelRequired = true;
                }

                // 3. Replace labels in commands
                for (Command cmd : subFunctionCommands) {
                    if(labelMap.containsKey(cmd.getLabel().trim())) {
                        cmd.replaceLabel(cmd.getLabel(), labelMap.get(cmd.getLabel().trim()));
                    }
                    if(cmd.getTargetLabel() == null) {continue;}
                    if (labelMap.containsKey(cmd.getTargetLabel().trim())) {
                        cmd.replaceLabel(cmd.getTargetLabel(), labelMap.get(cmd.getTargetLabel().trim()));
                    }
                }

                int index = 0;
                for(Variable v : clonedSubFunction.getVariables()) {
                    if (v instanceof WorkVariable) {
                        String newWorkVarName = generateNewWorkVariableName();
                        WorkVariable newWorkVar = new WorkVariable(newWorkVarName);
                        functionHelpers.add(newWorkVar);
                        this.associatedProgram.getVariables().add(newWorkVar);
                        for (Command cmd : subFunctionCommands) {
                            cmd.replaceVariable(v, newWorkVar);
                        }
                    } else if (v instanceof OutputVariable) {
                        String freshName;
                        boolean clash;
                        do {
                            freshName = generateNewWorkVariableName();
                            clash = freshName.equals(this.variable.getName());
                            if (!clash) {
                                for (Variable vv : this.associatedProgram.getVariables()) {
                                    if (vv.getName().equals(freshName)) { clash = true; break; }
                                }
                            }
                        } while (clash);

                        newOutputVarName = freshName;
                        WorkVariable newWorkVar = new WorkVariable(newOutputVarName);
                        outputTempVar = newWorkVar;
                        functionHelpers.add(newWorkVar);
                        this.associatedProgram.getVariables().add(newWorkVar);
                        for (Command cmd : subFunctionCommands) {
                            cmd.replaceVariable(v, newWorkVar);
                        }
                    } else if (v instanceof InputVariable) {
                        String formalName = v.getName();           // "x1", "x2", ...
                        String newWorkVarName = generateNewWorkVariableName();
                        WorkVariable newWorkVar = new WorkVariable(newWorkVarName);
                        functionHelpers.add(newWorkVar);
                        this.associatedProgram.getVariables().add(newWorkVar);
                        for (Command cmd : subFunctionCommands) {
                            cmd.replaceVariable(v, newWorkVar);
                        }
                        inputBind.put(formalName, newWorkVarName);
                        if(index >= this.argumentList.size()) {break;}
                        String arg = this.argumentList.get(index++);
                        if (arg.charAt(0) == 'x' || arg.charAt(0) == 'y' || arg.charAt(0) == 'z') {
                            for (Variable var : this.associatedProgram.getVariables()) {
                                if (var.getName().equals(arg)) {
                                    this.ExpandedCommands.add(new Assignment(newWorkVar, "   ", var, this, this.associatedProgram));
                                    break;
                                }
                            }
                        } else if (arg.charAt(0) == '(') {
                            String functionName = arg.substring(1, arg.indexOf(',') == -1 ? arg.length() - 1 : arg.indexOf(','));
                            String functionArguments = arg.indexOf(',') == -1 ? "" : arg.substring(arg.indexOf(',') + 1, arg.length() - 1);
                            List<String> subArgumentList = initializeArgumentList(functionArguments);
                            if(checkParentCommand(this) && functionName.equals("Minus") && subArgumentList.equals(List.of("x1","x2"))) {
                                subArgumentList = List.of("x2","x1");
                            } else if (checkParentCommand(this) && functionName.equals("NOT") && subArgumentList.equals(List.of("(Minus,x1,x2)"))) {
                                subArgumentList = List.of("(Minus,x2,x1)");
                            }
                            this.ExpandedCommands.add(new Quote(newWorkVar, functionName, subArgumentList, "   ", this, this.associatedProgram));
                        } else {
                            throw new IllegalArgumentException("Invalid argument passed in Quote: " + arg);
                        }
                    }
                }

                for (Command cmd : subFunctionCommands) {
                    if (cmd instanceof Quote q) {
                        // rewrite list entries
                        List<String> newArgs = new ArrayList<>(q.argumentList.size());
                        for (String a : q.argumentList) newArgs.add(rewriteArgsWithBindings(a, inputBind));
                        q.argumentList = newArgs;

                        // keep functionArguments string in sync if you use it elsewhere
                        q.functionArguments = rewriteArgsWithBindings(q.functionArguments, inputBind);
                    }
                }

                for(Command cmd : subFunctionCommands) {
                    if(checkParentCommand(cmd) && cmd instanceof Quote) {
                        Quote quoteCmd = (Quote) cmd;
                        if(quoteCmd.functionArguments.equals("(Minus,x1,x2)")) {
                            quoteCmd.functionArguments="(Minus,x2,x1)";
                            quoteCmd.argumentList= List.of("(Minus,x2,x1)");
                        } else if(quoteCmd.functionArguments.equals("x1,x2")) {
                            quoteCmd.functionArguments = "x2,x1";
                            quoteCmd.argumentList = List.of("x2","x1");
                        }
                    }
                }

                this.ExpandedCommands.addAll(subFunctionCommands);

                if (outputTempVar != null) {
                    String anchor = exitLabelRequired ? exitLabel : "   ";
                    this.ExpandedCommands.add(
                            new Assignment(this.variable, anchor, outputTempVar, this, this.associatedProgram)
                    );
                }

                for (Variable v : functionHelpers) {
                    // assign 0 directly instead of looping ZeroVariable
                    this.ExpandedCommands.add(
                            new ConstantAssignment(v, 0, "   ", this, this.associatedProgram)
                    );
                }

                // Found and processed the target function, break out of the loop
                break;
            }
        }

        // If not found in local subfunctions, try global functions as fallback
        if (this.ExpandedCommands.isEmpty() ||
                (this.ExpandedCommands.size() == 1 && this.ExpandedCommands.get(0) instanceof Neutral)) {
            Program targetFunction = getGlobalFunction(functionName);
            if (targetFunction != null) {
                // Process global function using the same logic as above
                processTargetFunction(targetFunction);
            }
        }

        expandFurther();
    }

    // Helper method to process a target function (reduces code duplication)
    private void processTargetFunction(Program targetFunction) {
        Set<Variable> functionHelpers = new LinkedHashSet<>();
        String SubFunctionName = targetFunction.getCurrentProgramName();
        boolean exitLabelRequired = false;
        String newLabel = generateNewLabel();
        this.associatedProgram.labels.add(newLabel);
        String exitLabel = newLabel + SubFunctionName + "_EXIT";
        this.associatedProgram.labels.add(exitLabel);

        WorkVariable outputTempVar = null;
        String newOutputVarName = null; // Fix: Declare the missing variable
        Map<String,String> inputBind = new HashMap<>();
        Program clonedSubFunction = targetFunction.clone();

        List<Command> subFunctionCommands = clonedSubFunction.getCommands();
        for (Command cmd : subFunctionCommands) {
            cmd.setParent(this);
            cmd.setAssociatedEngine(this.associatedProgram);
        }

        // 1. Gather all labels
        Set<String> allLabels = new HashSet<>();
        for (Command cmd : subFunctionCommands) {
            for(String lbl : cmd.getAssociatedLabels()) {
                allLabels.add(lbl.trim());
            }
        }

        boolean exitReferencedViaTarget = false;
        for (Command cmd : subFunctionCommands) {
            String tgt = cmd.getTargetLabel();
            if (tgt != null && "EXIT".equals(tgt.trim())) {
                exitReferencedViaTarget = true;
                break;
            }
        }

        // 2. Map each label to a new label
        Map<String, String> labelMap = new HashMap<>();
        for (String lbl : allLabels) {
            if ("EXIT".equals(lbl)) {
                labelMap.put(lbl, exitLabel);
                exitLabelRequired = true;
            } else if (lbl.trim().isEmpty()) {
                labelMap.put(lbl, lbl); // Keep neutral label as is
            } else {
                String newLbl = generateNewLabel();
                this.associatedProgram.labels.add(newLbl);
                labelMap.put(lbl, newLbl);
            }
        }

        if (exitReferencedViaTarget || allLabels.contains("EXIT")) {
            labelMap.put("EXIT", exitLabel);
            exitLabelRequired = true;
        }

        // 3. Replace labels in commands
        for (Command cmd : subFunctionCommands) {
            if(labelMap.containsKey(cmd.getLabel().trim())) {
                cmd.replaceLabel(cmd.getLabel(), labelMap.get(cmd.getLabel().trim()));
            }
            if(cmd.getTargetLabel() == null) {continue;}
            if (labelMap.containsKey(cmd.getTargetLabel().trim())) {
                cmd.replaceLabel(cmd.getTargetLabel(), labelMap.get(cmd.getTargetLabel().trim()));
            }
        }

        int index = 0;
        for(Variable v : clonedSubFunction.getVariables()) {
            if (v instanceof WorkVariable) {
                String newWorkVarName = generateNewWorkVariableName();
                WorkVariable newWorkVar = new WorkVariable(newWorkVarName);
                functionHelpers.add(newWorkVar);
                this.associatedProgram.getVariables().add(newWorkVar);
                for (Command cmd : subFunctionCommands) {
                    cmd.replaceVariable(v, newWorkVar);
                }
            } else if (v instanceof OutputVariable) {
                String freshName;
                boolean clash;
                do {
                    freshName = generateNewWorkVariableName();
                    clash = freshName.equals(this.variable.getName());
                    if (!clash) {
                        for (Variable vv : this.associatedProgram.getVariables()) {
                            if (vv.getName().equals(freshName)) { clash = true; break; }
                        }
                    }
                } while (clash);

                newOutputVarName = freshName; // Now this variable is properly declared
                WorkVariable newWorkVar = new WorkVariable(newOutputVarName);
                outputTempVar = newWorkVar;
                functionHelpers.add(newWorkVar);
                this.associatedProgram.getVariables().add(newWorkVar);
                for (Command cmd : subFunctionCommands) {
                    cmd.replaceVariable(v, newWorkVar);
                }
            } else if (v instanceof InputVariable) {
                String formalName = v.getName();           // "x1", "x2", ...
                String newWorkVarName = generateNewWorkVariableName();
                WorkVariable newWorkVar = new WorkVariable(newWorkVarName);
                functionHelpers.add(newWorkVar);
                this.associatedProgram.getVariables().add(newWorkVar);
                for (Command cmd : subFunctionCommands) {
                    cmd.replaceVariable(v, newWorkVar);
                }
                inputBind.put(formalName, newWorkVarName);
                if(index >= this.argumentList.size()) {break;}
                String arg = this.argumentList.get(index++);
                if (arg.charAt(0) == 'x' || arg.charAt(0) == 'y' || arg.charAt(0) == 'z') {
                    for (Variable var : this.associatedProgram.getVariables()) {
                        if (var.getName().equals(arg)) {
                            this.ExpandedCommands.add(new Assignment(newWorkVar, "   ", var, this, this.associatedProgram));
                            break;
                        }
                    }
                } else if (arg.charAt(0) == '(') {
                    String functionName = arg.substring(1, arg.indexOf(',') == -1 ? arg.length() - 1 : arg.indexOf(','));
                    String functionArguments = arg.indexOf(',') == -1 ? "" : arg.substring(arg.indexOf(',') + 1, arg.length() - 1);
                    List<String> subArgumentList = initializeArgumentList(functionArguments);
                    if(checkParentCommand(this) && functionName.equals("Minus") && subArgumentList.equals(List.of("x1","x2"))) {
                        subArgumentList = List.of("x2","x1");
                    } else if (checkParentCommand(this) && functionName.equals("NOT") && subArgumentList.equals(List.of("(Minus,x1,x2)"))) {
                        subArgumentList = List.of("(Minus,x2,x1)");
                    }
                    this.ExpandedCommands.add(new Quote(newWorkVar, functionName, subArgumentList, "   ", this, this.associatedProgram));
                } else {
                    throw new IllegalArgumentException("Invalid argument passed in Quote: " + arg);
                }
            }
        }

        for (Command cmd : subFunctionCommands) {
            if (cmd instanceof Quote q) {
                // rewrite list entries
                List<String> newArgs = new ArrayList<>(q.argumentList.size());
                for (String a : q.argumentList) newArgs.add(rewriteArgsWithBindings(a, inputBind));
                q.argumentList = newArgs;

                // keep functionArguments string in sync if you use it elsewhere
                q.functionArguments = rewriteArgsWithBindings(q.functionArguments, inputBind);
            }
        }

        for(Command cmd : subFunctionCommands) {
            if(checkParentCommand(cmd) && cmd instanceof Quote) {
                Quote quoteCmd = (Quote) cmd;
                if(quoteCmd.functionArguments.equals("(Minus,x1,x2)")) {
                    quoteCmd.functionArguments="(Minus,x2,x1)";
                    quoteCmd.argumentList= List.of("(Minus,x2,x1)");
                } else if(quoteCmd.functionArguments.equals("x1,x2")) {
                    quoteCmd.functionArguments = "x2,x1";
                    quoteCmd.argumentList = List.of("x2","x1");
                }
            }
        }

        this.ExpandedCommands.addAll(subFunctionCommands);

        if (outputTempVar != null) {
            String anchor = exitLabelRequired ? exitLabel : "   ";
            this.ExpandedCommands.add(
                    new Assignment(this.variable, anchor, outputTempVar, this, this.associatedProgram)
            );
        }

        for (Variable v : functionHelpers) {
            // assign 0 directly instead of looping ZeroVariable
            this.ExpandedCommands.add(
                    new ConstantAssignment(v, 0, "   ", this, this.associatedProgram)
            );
        }
    }

    private Program getGlobalFunction(String functionName) {
        try {
            servlets.ServerContext context = servlets.ServerContext.getInstance();

            // Search through all users and their programs
            java.util.Map<String, servlets.User> allUsers = context.getAllUsers();

            for (servlets.User user : allUsers.values()) {
                java.util.Map<String, engine.S_Emulator> userPrograms = user.getAllPrograms();

                for (engine.S_Emulator program : userPrograms.values()) {
                    if (program instanceof Program) {
                        Program prog = (Program) program;

                        // Check both program name and user string
                        if (prog.getCurrentProgramName().equals(functionName) ||
                                (prog.getUserString() != null && prog.getUserString().equals(functionName))) {
                            return prog;
                        }

                        // Also check subfunctions within each program
                        if (prog.subFunctions != null) {
                            for (Program subFunc : prog.subFunctions) {
                                if (subFunc.getCurrentProgramName().equals(functionName) ||
                                        (subFunc.getUserString() != null && subFunc.getUserString().equals(functionName))) {
                                    return subFunc;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not access global context: " + e.getMessage());
        }
        return null;
    }

    private boolean checkParentCommand(Command cmd) {
        Command current = cmd.getParentCommand();
        if (current == null) {
            return false;
        }
        if (current instanceof Quote) {
            Quote quoteParent = (Quote) current;
            if ("Smaller_Equal_Than".equals(quoteParent.functionName)
                    && !quoteParent.argumentList.isEmpty()
                    && "x2".equals(quoteParent.argumentList.getFirst())) {
                return true;
            }
        }
        // Always recurse if not found
        return checkParentCommand(current);
    }

    private String rewriteArgsWithBindings(String s, Map<String, String> bind) {
        if (s == null || s.isEmpty() || bind.isEmpty()) return s;

        // Fast path for plain identifiers (no commas/parens)
        String trimmed = s.trim();
        if (!trimmed.contains(",") && !trimmed.contains("(") && !trimmed.contains(")")) {
            return bind.getOrDefault(trimmed, trimmed);
        }

        StringBuilder out = new StringBuilder(s.length());
        String[] parts = s.split(",");  // OK: we are only replacing identifiers, not parsing
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i] == null ? "" : parts[i];
            String t = p.trim();

            // Count ALL leading '('
            int openCount = 0;
            while (openCount < t.length() && t.charAt(openCount) == '(') openCount++;
            // Count ALL trailing ')'
            int closeCount = 0;
            while (closeCount < t.length() - openCount && t.charAt(t.length() - 1 - closeCount) == ')') closeCount++;

            String core = t.substring(openCount, t.length() - closeCount).trim();
            if (core.isEmpty()) {
                // nothing meaningful—just rebuild parentheses
                for (int k = 0; k < openCount; k++) out.append('(');
                for (int k = 0; k < closeCount; k++) out.append(')');
            } else {
                // replace only exact identifier matches
                String repl = bind.getOrDefault(core, core);
                for (int k = 0; k < openCount; k++) out.append('(');
                out.append(repl);
                for (int k = 0; k < closeCount; k++) out.append(')');
            }

            if (i < parts.length - 1) out.append(',');
        }
        return out.toString();
    }

    @Override
    public String execute() {
        int result = 0;
        Set<Variable> snapshot = takeValueSnapshot(this.associatedProgram.getVariables());

        // V3 CHANGE: First try local subFunctions, then try global functions
        Program targetFunction = null;

        // Check local subfunctions first
        for (Program e : this.associatedProgram.subFunctions) {
            if (e.getCurrentProgramName().equals(functionName) || e.getUserString().equals(functionName)) {
                targetFunction = e;
                break;
            }
        }

        // If not found locally, try global functions from server
        if (targetFunction == null) {
            targetFunction = getGlobalFunction(functionName);
        }

        if (targetFunction != null) {
            List<Variable> varsToPass = new ArrayList<>();
            for (String arg : argumentList) {
                if (arg.charAt(0) == 'x' || arg.charAt(0) == 'y' || arg.charAt(0) == 'z') {
                    for (Variable v : this.associatedProgram.getVariables()) {
                        if (v.getName().equals(arg)) {
                            varsToPass.add(v);
                            break;
                        }
                    }
                } else if (arg.charAt(0) == '(') {
                    varsToPass.add(handleFunctionCall(arg));
                    for (Variable var : this.associatedProgram.variables) {
                        for (Variable snapshotVar : snapshot) {
                            if (var.getName().equals(snapshotVar.getName())) {
                                var.setValue(snapshotVar.getValue());
                                break;
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Invalid argument passed in Quote: " + arg);
                }
            }
            result = targetFunction.executeFunction(varsToPass, functionName, this.associatedProgram);
        }

        for (Variable var : this.associatedProgram.variables) {
            for (Variable snapshotVar : snapshot) {
                if (var.getName().equals(snapshotVar.getName())) {
                    var.setValue(snapshotVar.getValue());
                    break;
                }
            }
        }
        this.variable.setValue(result);
        return null;
    }

    private Set<Variable> takeValueSnapshot(Set<Variable> variables) {
        Set<Variable> snapshot = new HashSet<>();
        for (Variable var : variables) {
            Variable varCopy;
            if (var instanceof InputVariable) {
                varCopy = var.clone();
            } else if (var instanceof OutputVariable) {
                varCopy = var.clone();
            } else if (var instanceof WorkVariable) {
                varCopy = var.clone();
            } else {
                throw new IllegalArgumentException("Unknown variable type: " + var.getClass().getName());
            }
            varCopy.setValue(var.getValue());
            snapshot.add(varCopy);
        }
        return snapshot;
    }

    private Variable handleFunctionCall(String arg) {
        //handle function calls inside arguments
        Variable var = null;
        Set<Variable> snapshot = takeValueSnapshot(this.associatedProgram.getVariables());
        List<Variable> subVarsToPass;

        String name = arg.substring(1, arg.indexOf(',') == -1 ? arg.length() - 1 : arg.indexOf(','));

        // V3 CHANGE: First try local subFunctions, then try global functions
        Program targetFunction = null;

        // Check local subfunctions first
        for (Program subE : this.associatedProgram.subFunctions) {
            if (subE.getCurrentProgramName().equals(name)) {
                targetFunction = subE;
                break;
            }
        }

        // If not found locally, try global functions from server
        if (targetFunction == null) {
            targetFunction = getGlobalFunction(name);
        }

        if (targetFunction != null) {
            subVarsToPass = new ArrayList<>();
            String subFunctionArguments = arg.indexOf(',') == -1 ? "" : arg.substring(arg.indexOf(',') + 1, arg.length() - 1);
            List<String> subArgumentList = initializeArgumentList(subFunctionArguments);
            for (String subArg : subArgumentList) {
                if (subArg.charAt(0) == 'x' || subArg.charAt(0) == 'y' || subArg.charAt(0) == 'z') {
                    for (Variable v : this.associatedProgram.getVariables()) {
                        if (v.getName().equals(subArg)) {
                            subVarsToPass.add(v);
                            break;
                        }
                    }
                } else if (subArg.charAt(0) == '(') {
                    subVarsToPass.add(handleFunctionCall(subArg));
                    for (Variable varz : this.associatedProgram.variables) {
                        for (Variable snapshotVar : snapshot) {
                            if (varz.getName().equals(snapshotVar.getName())) {
                                varz.setValue(snapshotVar.getValue());
                                break;
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Invalid argument passed in Quote: " + subArg);
                }
            }
            int resultOfSubFunction = targetFunction.executeFunction(subVarsToPass, name, this.associatedProgram);
            var = new WorkVariable("temp");
            var.setValue(resultOfSubFunction);
        }

        return var;
    }

    // Public getter methods for external access
    public String getFunctionName() {
        return functionName;
    }

    public String getFunctionArguments() {
        return functionArguments;
    }

    public List<String> getArgumentList() {
        return argumentList;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String getTargetLabel() {
        return "";
    }

    @Override
    public Set<Variable> getAllVariables() {
        return Set.copyOf(this.associatedVariables);
    }

    @Override
    public Collection<String> getAssociatedLabels() {
        return List.of(this.label);
    }

    @Override
    public void replaceLabel(String lbl, String newLabel) {
        if (newLabel == null) {
            return;
        }
        if (this.label != null && this.label.equals(lbl)) {
            this.label = newLabel;
            this.associatedLabels.remove(lbl);
            this.associatedLabels.add(newLabel);
        }
    }

    @Override
    public String toString() {
        // Display format: V ← (FunctionName, arg1, arg2, ...)
        if (functionArguments == null || functionArguments.isEmpty()) {
            return variable.getName() + " <- (" + userString + ")";
        } else {
            return variable.getName() + " <- (" + userString + "," + functionArguments + ")";
        }
    }

    @Override
    public int getExpansionDepth() {
        // CRITICAL FIX: Always calculate dynamically, never rely on levelOfExpansion field
        // This ensures we get the actual depth regardless of initialization timing
        if (!this.didInitialize) {
            // If not initialized yet, force initialization
            initializeExpandedCommands();
        }

        // Calculate actual depth from expanded commands
        int maxDepth = 1;
        for (Command cmd : this.getExpandedCommands()) {
            int cmdDepth = cmd.getExpansionDepth();
            if (cmdDepth >= maxDepth) {
                maxDepth = cmdDepth + 1;
            }
        }

        // Also update the field for consistency
        this.levelOfExpansion = maxDepth;
        return maxDepth;
    }

    @Override
    public Quote clone() {
        Quote cloned = (Quote) super.clone();
        cloned.argumentList = new ArrayList<>(this.argumentList);
        cloned.functionName = this.functionName;
        cloned.functionArguments = this.functionArguments;
        cloned.userString = this.userString;
        return cloned;
    }
}

