package engine.commands.base.types;

import engine.commands.base.BaseCommand;

public class JumpNotZero extends BaseCommand {
    public JumpNotZero(){
        super();
        this.commandName = "JUMP_NOT_ZERO";
        this.cycles = 2; // Assuming it takes 1 cycle to execute
    }

    public String jumpNotZero(int V, String L) {
        if (V != 0) {
            return L; // Return label L if V is not zero
        } else {
            return "  "; // Return empty label if V is zero
        }
    }
}
