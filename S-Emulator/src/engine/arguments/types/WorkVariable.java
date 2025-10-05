package engine.arguments.types;

import engine.arguments.Variable;

import java.io.Serializable;

public class WorkVariable extends Variable implements Serializable {
    boolean isForGotoLabel = false; // Flag to indicate if this variable is used for GOTO label

    public WorkVariable(String name) {
        this.id = name.charAt(1) - '0'; // Extracting ID from the name
        this.type = 'z';
        this.name = name;
    }

    public boolean isForGotoLabel() {
        return isForGotoLabel;
    }

    public void setForGotoLabel(boolean isForGotoLabel) {
        this.isForGotoLabel = isForGotoLabel;
    }
}
