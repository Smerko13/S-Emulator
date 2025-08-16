package ui;

import engine.S_Emulator;
import engine.commands.Command;

import java.util.Scanner;

public class Menu {
    private Scanner scanner;

    public Menu() {
        this.scanner = new Scanner(System.in);
    }

    public int displayMenu() {
        int choice = 0;
        do {
            System.out.println("Please select an option: [Type the number and press Enter]");
            System.out.println("--------------------------------------------------");
            System.out.println("    1. Load Program");
            System.out.println("    2. Show Program");
            System.out.println("    3. Expand Program");
            System.out.println("    4. Run Program");
            System.out.println("    5. Show statistics/history");
            System.out.println("    6. Exit");
            System.out.println("--------------------------------------------------");
            System.out.print("Enter your choice here: ");
            choice = scanner.nextInt();
            if (choice < 1 || choice > 6) {
                System.out.println("Invalid choice. Please try again.");
            }
        } while (choice < 1 || choice > 6);
        return choice;
    }

    public void displayWelcomeMessage() {
        System.out.println("Welcome to S-Emulator!");
    }

    public void displayGoodbyeMessage() {
        System.out.println("Thank you for using S-Emulator!");
        System.out.println("Exiting the program. Goodbye!");
    }

    public void showProgram(S_Emulator engine) {
        System.out.println("Program details:");
        System.out.println("    Program Name: " + engine.getCurrentProgramName());
        System.out.println("    Input parameters: " + engine.getListOfInputParameters());
        System.out.println("    Labels: " + engine.getLabels());
        System.out.println("    Commands: ");
        for (Command command : engine.getCommands()) {
            if( command == null) {
                continue; // Skip null commands
            }
            System.out.println("        " + command.getCommandRepresentation());
        }
    }

    public String getFilePath() {
        System.out.print("Enter the full file path of the program (must be an xml file): ");
        return scanner.next();
    }

    public void displayLoadStatus(boolean success) {
        if (success) {
            System.out.println("Program loaded successfully!");
        } else {
            displayFailedToLoadMessage();
        }
    }
    public void displayFailedToLoadMessage() {
        System.out.println("Failed to load the program. Please check the file path and format.");
    }

    public boolean validatePath(String filePath) {
        return filePath != null && filePath.endsWith(".xml");
    }

    public int getExpansionLevel(S_Emulator engine) {
        int maxExpansionLevel = engine.getMaxExpansionDepth();
        int expansionLevel = 0;
        do {
            System.out.print("Enter the expansion level (0 to " + maxExpansionLevel + "): ");
            expansionLevel = scanner.nextInt();
            if (expansionLevel < 0 || expansionLevel > maxExpansionLevel) {
                System.out.println("Invalid expansion level. Please try again.");
            }
        } while (expansionLevel < 0 || expansionLevel > maxExpansionLevel);
        return expansionLevel;
    }

    public void showInputVariables(S_Emulator engine) {
        String inputVariables = engine.getListOfInputParameters();
        System.out.println("Input Variables:");
        if (inputVariables.isEmpty()) {
            System.out.println("    No input variables found.");
        } else {
            System.out.println(inputVariables);
        }
    }

    public void getInputVariablesValues(S_Emulator engine) {
        System.out.println("Please enter values for the input variables:");
        System.out.println("(A list of numbers separated by the character (,) eg: 1,2,3)");
        String input = scanner.next();
        String[] values = input.split(",");
        engine.SetInputVariablesValues(values);

    }
}
