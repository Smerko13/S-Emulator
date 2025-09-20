package engine.commands.synthetic.types;

import engine.Engine;
import engine.arguments.Variable;
import engine.commands.Command;
import engine.commands.base.types.Decrease;
import engine.commands.base.types.JumpNotZero;
import engine.commands.base.types.Neutral;
import engine.commands.synthetic.SyntheticCommand;
import schema.SInstruction;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;


public class ZeroVariable extends SyntheticCommand implements Serializable {

    public ZeroVariable(SInstruction instruction, Engine engine) {
        super(instruction, engine);
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.levelOfExpansion = 1;
    }

    public ZeroVariable(Variable variable, String label, Command parentCommand, Engine engine) {
        super(variable, label, parentCommand, engine);
        this.commandName = "ZERO_VARIABLE";
        this.cycles = 1;
        this.levelOfExpansion = 1;
    }

    public void initializeExpandedCommands() {
        String newLabel = generateNewLabel();
        Engine.labels.add(newLabel);
        if(this.label.equals("   ")) {
            this.ExpandedCommands.add(new Decrease(this.variable,newLabel, this, this.associatedEngine));
        } else {
            this.ExpandedCommands.add(new Neutral(this.variable,this.label, this, this.associatedEngine));
            this.ExpandedCommands.add(new Decrease(this.variable,newLabel, this, this.associatedEngine));
        }
        this.ExpandedCommands.add(new JumpNotZero(this.variable,newLabel,"   ", this, this.associatedEngine));
        expandFurther();
    }

    @Override
    public String execute() {
        this.variable.setValue(0);
        return null;
    }

    @Override
    public boolean isValid() {
        return variable != null;
    }

    @Override
    public String getTargetLabel() {
        return null;
    }

    @Override
    public Set<Variable> getAllVariables() {
        return Collections.singleton(this.variable);
    }



    @Override
    public String toString() {
        return variable.getName() + " <- 0";
    }
}
