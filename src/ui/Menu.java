package ui;

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
}
