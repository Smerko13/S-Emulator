package ui;

import engine.Engine;
import engine.S_Emulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UI {
    Menu menu;
    S_Emulator engine;
    boolean exit = false;
    boolean readFile = false;
    String filePath;


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
                    try {
                        saveCurrentProgram(readFile);
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 7:
                    loadSavedProgram();
                    break;
                case 8:
                    exit = true;
                    menu.displayGoodbyeMessage();
                    break;
            }
            if (!exit) {
                choice = menu.displayMenu();
            }
        }
    }

    private void loadSavedProgram() {
        Scanner scanner = new java.util.Scanner(System.in);
        System.out.println("Enter the full path of the saved program file (no extensions):");
        try {
            filePath = scanner.nextLine();
            File file = new File(filePath);
            if (!file.exists()) {
                throw new FileNotFoundException("File " + filePath + " does not exist.");
            }
        } catch (InputMismatchException | FileNotFoundException e) {
            System.out.println("Invalid input. Please enter a valid file path and validate path exists.");
            return;
        }

        try {
            engine = S_Emulator.loadSavedProgram(filePath );
        } catch (RuntimeException e) {
            engine = null;
        }

        if (engine != null) {
            readFile = true;
            menu.displayLoadStatus(true);
        } else {
            menu.displayLoadStatus(false);
        }
    }

    private void saveCurrentProgram(boolean readFile) {
        if (readFile) {
            System.out.println("Enter the full path where you want to save the program:");
            Scanner scanner = new java.util.Scanner(System.in);
            try {
                filePath = scanner.nextLine();
                File file = new File(filePath);
                if (!file.exists()) {
                    throw new FileNotFoundException("Path " + filePath + " does not exist.");
                }
            } catch (java.util.InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid file path and validate path exists.");
                return;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            engine.saveCurrentProgram(filePath);
        } else {
            System.out.println("No program loaded to save.");
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
            menu.showInputVariables(engine, expansionLevel);
            menu.getInputVariablesValues(engine);
            engine.executeProgram(expansionLevel);
            System.out.println("*****The program that was executed:*****");
            menu.showProgram(engine, expansionLevel);
            menu.showOutputVariable(engine);
            menu.displayVariables(engine,expansionLevel);
            menu.displayCycleSum(engine);
            engine.reset();
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
            menu.showProgram(engine, 0);
        } else {
            menu.displayFailedToLoadMessage();
        }
    }

    private boolean loadProgram() {
        try{
        String filePath = menu.getFilePath();
        boolean readFile = menu.validatePath(filePath);
        if(readFile) {
            engine = new engine.Engine();
            readFile = engine.readProgramFromXml(filePath);
        }
        menu.displayLoadStatus(readFile);
        return readFile;
        } catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }

    }
}
