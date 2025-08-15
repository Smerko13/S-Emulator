package engine.commands.synthetic.types;

import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class Assignment extends SyntheticCommand {
    protected engine.arguments.Varible assignedVarible;
    public Assignment(SInstruction instruction) {
        super();
        this.commandName = "ASSIGNMENT";
        if (instruction.getSLabel() != null) {this.label = instruction.getSLabel();}
        this.cycles = 4; // Assuming assignment takes 1 cycle
        String var = instruction.getSVariable();
        this.varible = extractVariables(var, this.varible);
        String assignedVar = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.assignedVarible = extractVariables(assignedVar, this.assignedVarible);
        this.levelOfExpansion = 2;
        initializeExpandedCommands();
    }

    @Override
    public void initializeExpandedCommands() {

    }

    @Override
    public String toString() {
        return varible.getName() + " <- " + assignedVarible.getName();
    }
}
