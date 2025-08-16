package engine.arguments.types;

import engine.arguments.Varible;

public class WorkVarible extends Varible {
    boolean isForGotoLabel = false; // Flag to indicate if this variable is used for GOTO label

    public WorkVarible(String name) {
        this.id = name.charAt(1) - '0'; // Extracting ID from the name
        this.type = 'z';
        this.name = name;
    }

    public boolean isForGotoLabel() {
        return isForGotoLabel;
    }
}
