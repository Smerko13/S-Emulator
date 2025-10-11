package servlets;

import engine.S_Emulator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class User {
    private final String userName;
    private int credits;
    private final Map<String, S_Emulator> programs; // programName -> S_Emulator

    public User(String userName, int initialCredits) {
        this.userName = userName;
        this.credits = initialCredits;
        this.programs = new ConcurrentHashMap<>();
    }

    public User(String userName) {
        this(userName, 100); // Default starting credits
    }

    // User name methods
    public String getUserName() {
        return userName;
    }

    // Credits methods
    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = Math.max(0, credits); // Prevent negative credits
    }

    public boolean hasEnoughCredits(int requiredCredits) {
        return credits >= requiredCredits;
    }

    public boolean deductCredits(int amount) {
        if (hasEnoughCredits(amount)) {
            credits -= amount;
            return true;
        }
        return false;
    }

    public void addCredits(int amount) {
        if (amount > 0) {
            credits += amount;
        }
    }

    // Program management methods
    public void addProgram(String programName, S_Emulator program) {
        programs.put(programName, program);
    }

    public S_Emulator getProgram(String programName) {
        return programs.get(programName);
    }

    public Map<String, S_Emulator> getAllPrograms() {
        return new ConcurrentHashMap<>(programs);
    }

    public boolean hasProgram(String programName) {
        return programs.containsKey(programName);
    }

    public void removeProgram(String programName) {
        programs.remove(programName);
    }

    public int getProgramCount() {
        return programs.size();
    }

    // Utility methods
    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", credits=" + credits +
                ", programCount=" + programs.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userName.equals(user.userName);
    }

    @Override
    public int hashCode() {
        return userName.hashCode();
    }
}
