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
        while (!exit) {
            switch (choice) {
                case 1:
                    engine.readProgramFromXml();
                    break;
                case 2:
                    showProgram(this.engine);
                    break;
                case 3:
                    break;
                case 4:
                    executeProgram(this.engine);
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

    private void executeProgram(S_Emulator engine) {
        System.out.println("Maximum expansion depth: " + engine.getMaxExpansionDepth());

    }

    private void showProgram(S_Emulator engine) {
        System.out.println("Program details:");
        System.out.println("    Program Name: " + engine.getCurrentProgramName());
        System.out.println("    Input parameters: " + engine.getListOfInputParameters());
        System.out.println("    Labels: " + engine.getLabels());
        System.out.println("    Commands: ");
        for (Command command : engine.getCommands()) {
            System.out.println("        " + command.getCommandRepresentation());
        }

    }
}
