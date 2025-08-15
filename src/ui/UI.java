package ui;

import engine.Engine;
import engine.S_Emulator;
import engine.commands.Command;

public class UI {
    Menu menu;
    S_Emulator engine;

    public UI(S_Emulator engine) {
        this.engine = engine;
        this.menu = new Menu();
    }

    public void start() {
        menu.displayWelcomeMessage();
        int choice = menu.displayMenu();
        boolean exit = false;
        boolean readFile = false;
        while (!exit) {
            switch (choice) {
                case 1:
                    String filePath = menu.getFilePath();
                    readFile = menu.validatePath(filePath) && engine.readProgramFromXml(filePath);
                    menu.displayLoadStatus(readFile);
                    break;
                case 2:
                    if (readFile) {
                        menu.showProgram(engine);
                    } else {
                        menu.displayFailedToLoadMessage();
                    }
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
