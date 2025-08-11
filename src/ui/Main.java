package ui;

import engine.Engine;

public class Main {
    public static void main(String[] args) {
        Engine engine = new Engine();
        UI ui = new UI(engine);
        ui.start();
    }
}
