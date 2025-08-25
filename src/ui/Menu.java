package ui;

import engine.Engine;
import engine.S_Emulator;
import engine.Stats;
import engine.arguments.Variable;
import engine.arguments.types.InputVariable;
import engine.arguments.types.OutputVariable;
import engine.arguments.types.WorkVariable;
import engine.commands.Command;

import java.util.*;

public class Menu {
    private final Scanner scanner;

    public Menu() {
        this.scanner = new Scanner(System.in);
    }

    public int displayMenu() {
        int choice;
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

    public void showProgram(S_Emulator engine, int expansionLevel) {
        System.out.println("Program details:");
        System.out.println("    Program Name: " + engine.getCurrentProgramName());
        System.out.println("    Input parameters: " + engine.getListOfInputParameters(expansionLevel));
        System.out.println("    Labels: " + engine.getLabels(expansionLevel));
        System.out.println("    Commands: ");
        engine.arrangeIDs(expansionLevel);
        for (Command command : engine.getCommandsAtDesiredLevel(expansionLevel)) {
            if (command == null) {
                continue; // Skip null commands
            }
            System.out.println("        " + command.getCommandRepresentation());
        }
        System.out.println();
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
        int expansionLevel;
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

    public void showInputVariables(S_Emulator engine, int expansionLevel) {
        String inputVariables = engine.getListOfInputParameters(expansionLevel);
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
        if(input.isEmpty()) {
            return;
        }
        String[] values = input.split(",");
        engine.SetInputVariablesValues(values);
    }

    public void displayVariables(S_Emulator engine,int expansionLevel) {
        System.out.println("*****Other variables(After program execution):*****");
        Set<Variable> inputVariables = new LinkedHashSet<>();
        Set<Variable> workVariables = new LinkedHashSet<>();
        for(Command command : engine.getCommandsAtDesiredLevel(expansionLevel)) {
            if(command == null) {
                continue; // Skip null commands
            }
            for(Variable variable : command.getAssociatedVariables()) {
                if(variable instanceof InputVariable) {
                    inputVariables.add(variable);
                } else if(variable instanceof WorkVariable) {
                    workVariables.add(variable);
                } else {
                    continue;
                }
            }
        }

        List<Variable> sortedInputVars = new ArrayList<>(inputVariables);
        sortedInputVars.addAll(Engine.extraInputVariables);
        sortedInputVars.sort(Comparator.comparing(Variable::getValue));

        List<Variable> sortedWorkVars = new ArrayList<>(workVariables);
        sortedWorkVars.sort(Comparator.comparing(Variable::getValue));

        for (Variable variable : sortedInputVars) {
            System.out.println("    " + variable.getName() + " = " + variable.getValue());
        }
//        for (Variable variable : Engine.extraInputVariables) {
//            System.out.println("    " + variable.getName() + " = " + variable.getValue());
//        }
        for (Variable variable : sortedWorkVars) {
            System.out.println("    " + variable.getName() + " = " + variable.getValue());
        }
    }

    public void showOutputVariable(S_Emulator engine) {
        System.out.println("*****Output Variable(After program execution):*****");
        OutputVariable outputVarible = null;
        for (Variable variable : engine.getVariables()) {
            if (variable instanceof OutputVariable) {
                outputVarible = (OutputVariable) variable;
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

    public void showStatisticsHistory(S_Emulator engine) {
        System.out.println("*****Statistics/History:*****");
        Stats executionHistory = engine.getExecutionHistory();
        System.out.println(executionHistory.toString());
    }

    public void showExpandedProgram(S_Emulator engine) {
        int expansionLevel = getExpansionLevel(engine);
        List<Command> commandsAtLevel = engine.getCommandsAtDesiredLevel(expansionLevel);
        int maxCommandLength = findMaxCommandLength(engine, expansionLevel);
        String header = "Commands at level: " + expansionLevel;
        System.out.print(header);
        String indentation = " ".repeat((maxCommandLength - header.length()+1));
        if(expansionLevel !=0) {
            System.out.println(indentation + "| Chain of Expansion");
        }
        else {
            System.out.println();
        }
        System.out.println("-".repeat(maxCommandLength) + "-------------------");
        for (Command command : commandsAtLevel) {
            recursivePrint(command, maxCommandLength, expansionLevel,engine);
        }
        System.out.println();

    }

    private void recursivePrint(Command command, int maxCommandLength, int expansionLevel,S_Emulator engine) {
        if (command == null) {
            return; // Skip null commands
        }
        engine.arrangeIDs(expansionLevel);
        System.out.print(command.getCommandRepresentation());
        if(command.getParentCommand() != null) {
            int currentLength = command.getCommandRepresentation().length();
            for (int i = 0; i < maxCommandLength - currentLength + 1; i++) {
                System.out.print(" ");
            }
            System.out.print(" <<< ");
            recursivePrint(command.getParentCommand(), maxCommandLength, expansionLevel - 1,engine);
        } else {
            System.out.println();
        }
    }

    private int findMaxCommandLength(S_Emulator engine, int expansionLevel) {
        List<List<Command>> allCommands = new java.util.ArrayList<>();
        for(int i = 0 ; i <= expansionLevel ; i++) {
            allCommands.add(engine.getCommandsAtDesiredLevel(i));
        }
        int maxLength = 0;
        for (List<Command> commands : allCommands) {
            for (Command command : commands) {
                int length = command.getCommandRepresentation().length();
                if (length > maxLength) {
                    maxLength = length;
                }
            }
        }
        return maxLength;
    }
}