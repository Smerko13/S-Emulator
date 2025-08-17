package ui;

import engine.S_Emulator;
import engine.arguments.Varible;
import engine.arguments.types.InputVarible;
import engine.arguments.types.OutputVarible;
import engine.arguments.types.WorkVarible;
import engine.commands.Command;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

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
            System.out.println("        " + command.getCommandRepresentation(engine.getCommands().indexOf(command)+1));
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
        System.out.println("Maximum expansion level for this program is: " + maxExpansionLevel);
        do {
            System.out.print("Enter the desired expansion level (0 to " + maxExpansionLevel + "): ");
            expansionLevel = scanner.nextInt();
            if (expansionLevel < 0 || expansionLevel > maxExpansionLevel) {
                System.out.println("Invalid expansion level. Please try again.");
            }
        } while (expansionLevel < 0 || expansionLevel > maxExpansionLevel);
        return expansionLevel;
    }

    public void showInputVariables(S_Emulator engine) {
        String inputVariables = engine.getListOfInputParameters();
        System.out.println("*****Input Variables:*****");
        if (inputVariables.isEmpty()) {
            System.out.println("No input variables found.");
        } else {
            System.out.println(inputVariables);
        }
    }

    public void getInputVariablesValues(S_Emulator engine) {
        System.out.println("*****Please enter values for the input variables:*****");
        System.out.println("[A list of numbers separated by the character (,) eg: 1,2,3]");
        String input = scanner.next();
        String[] values = input.split(",");
        engine.SetInputVariablesValues(values);
    }

    public void displayVariables(S_Emulator engine) {
        System.out.println("*****Other variables(After program execution):*****");
        List<Varible> inputVariables = new java.util.ArrayList<>(engine.getVariables().stream()
                .filter(varible -> varible instanceof InputVarible)
                .toList());
        List<Varible> workVariables = new java.util.ArrayList<>(engine.getVariables().stream()
                .filter(varible -> varible instanceof WorkVarible)
                .toList());
        inputVariables.sort(Comparator.comparingInt(Varible::getValue));
        workVariables.sort(Comparator.comparingInt(Varible::getValue));

        for (Varible varible : inputVariables) {
            System.out.println("    " + varible.getName() + " = " + varible.getValue());
        }
        for (Varible varible : workVariables) {
            System.out.println("    " + varible.getName() + " = " + varible.getValue());
        }
    }

    public void showOutputVariable(S_Emulator engine) {
        System.out.println("*****Output Variable(After program execution):*****");
        OutputVarible outputVarible = null;
        for (Varible varible : engine.getVariables()) {
            if (varible instanceof OutputVarible) {
                outputVarible = (OutputVarible) varible;
                break;
            }
        }
        if (outputVarible != null) {
            System.out.println("    " + outputVarible.getName() + " = " + outputVarible.getValue());
        } else {
            System.out.println("No output variable found.");
        }

    }

    public void displayCycleSum(S_Emulator engine) {
        System.out.println("*****Total Cycles Executed:*****");
        int cycleSum = engine.getCycleSum();
        System.out.println("    Total Cycles Executed: " + cycleSum);
    }
}
