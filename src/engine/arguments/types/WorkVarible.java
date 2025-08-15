package engine.arguments.types;

import engine.arguments.Varible;

public class WorkVarible extends Varible {

    public WorkVarible(String name) {
        this.id = name.charAt(1) - '0'; // Extracting ID from the name
        this.type = 'z'; // 'W' for Work
        this.name = name;
    }
}
