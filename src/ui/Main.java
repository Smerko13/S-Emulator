package ui;

import engine.Engine;
import engine.S_Emulator;

public class Main {
    public static void main(String[] args) {
        S_Emulator engine = new Engine();
        UI ui = new UI(engine);
        ui.start();
    }
}
