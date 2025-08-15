package engine.arguments.types;

import engine.arguments.Varible;

public class InputVarible extends Varible {

    public InputVarible(String name) {
        this.id = name.charAt(1) - '0'; // Extracting ID from the name
        this.type = 'x'; // 'I' for Input
        this.name = name;
    }
}
