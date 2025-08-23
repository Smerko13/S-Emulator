package ui;

import engine.S_Emulator;

public class UI {
    Menu menu;
    S_Emulator engine;
    boolean exit = false;
    boolean readFile = false;


    public UI(S_Emulator engine) {
        this.engine = engine;
        this.menu = new Menu();
    }

    public void start() {
        menu.displayWelcomeMessage();
        int choice = menu.displayMenu();
        while (!exit) {
            switch (choice) {
                case 1:
                    readFile = loadProgram();
                    break;
                case 2:
                    displayProgram(readFile);
                    break;
                case 3:
                    expandProgram(readFile);
                    break;
                case 4:
                    executeProgram(readFile);
                    break;
                case 5:
                    showStats(readFile);
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

    private void showStats(boolean readFile) {
        if (readFile) {
            menu.showStatisticsHistory(engine);
        } else {
            menu.displayFailedToLoadMessage();
        }
    }

    private void executeProgram(boolean readFile) {
        if(readFile) {
            int expansionLevel = menu.getExpansionLevel(engine);
            menu.showInputVariables(engine);
            menu.getInputVariablesValues(engine);
            engine.executeProgram(expansionLevel);
            System.out.println("*****The program that was executed:*****");
            menu.showProgram(engine);
            menu.showOutputVariable(engine);
            menu.displayVariables(engine);
            menu.displayCycleSum(engine);
        }
        else {
            menu.displayFailedToLoadMessage();
        }
    }

    private void expandProgram(boolean readFile) {
        if (readFile) {
            menu.showExpandedProgram(engine);
        } else {
            menu.displayFailedToLoadMessage();
        }
    }

    private void displayProgram(boolean readFile) {
        if (readFile) {
            menu.showProgram(engine);
        } else {
            menu.displayFailedToLoadMessage();
        }
    }

    private boolean loadProgram() {
        String filePath = menu.getFilePath();
        boolean readFile = menu.validatePath(filePath) && engine.readProgramFromXml(filePath);
        menu.displayLoadStatus(readFile);
        return readFile;
    }
}
