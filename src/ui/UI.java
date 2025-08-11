package ui;

import engine.Engine;

public class UI {
    Menu menu;
    Engine engine;

    public UI(Engine engine) {
        this.engine = engine;
        this.menu = new Menu();
    }

    public void start() {
        menu.displayWelcomeMessage();
        int choice = menu.displayMenu();
        boolean exit = false;
        while (!exit) {
            switch (choice) {
                case 1:
                    engine.readProgramFromXml();
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    exit = true;
                    menu.displayGoodbyeMessage();
                    break;
            }
            if (!exit) {
                choice = menu.displayMenu();
            }
        }
    }
}
