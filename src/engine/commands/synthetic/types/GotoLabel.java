package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;
import engine.commands.base.types.Increase;
import engine.commands.base.types.JumpNotZero;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

public class GotoLabel extends SyntheticCommand {
    private final String gototLabel;

    public GotoLabel(SInstruction instruction) {
        super(instruction);
        this.commandName = "GOTO_LABEL";
        this.cycles = 1;
        this.levelOfExpansion = 1;
        gototLabel = instruction.getSInstructionArguments().getSInstructionArgument().getFirst().getValue();
        this.variable = createNewWorkVariable();
    }
    
    public GotoLabel(String gototLabel, Command parentCommand) {
        super(null,"   ", parentCommand);
        this.commandName = "GOTO_LABEL";
        this.cycles = 1;
        this.levelOfExpansion = 1;
        this.gototLabel = gototLabel;
        this.variable = createNewWorkVariable();
    }

    private Variable createNewWorkVariable() {
        String workVarName = generateNewWorkVariableName();
        WorkVariable workVariable = new WorkVariable(workVarName);
        Engine.variables.add(workVariable);
        workVariable.setForGotoLabel(true);
        return workVariable;
    }

    @Override
    public void initializeExpandedCommands() {
        this.ExpandedCommands.add(new Increase(this.variable, this.label,this));
        this.ExpandedCommands.add(new JumpNotZero(this.variable,this.gototLabel, "   ",this));
    }

    @Override
    public String execute() {
        this.variable.setValue(this.variable.getValue() + 1);
        return gototLabel;
    }

    @Override
    public String toString() {
        return "GOTO " + gototLabel;
    }
}
