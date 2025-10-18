package servlets;

import engine.S_Emulator;
import engine.chat.ChatManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ServerContext {
    private static volatile ServerContext instance;
    private final Map<String, User> users; // userId -> User
    private final Map<String, ProgramStats> globalProgramStats; // programName -> ProgramStats
    private final ChatManager chatManager; // Global chat manager

    private ServerContext() {
        this.users = new ConcurrentHashMap<>();
        this.globalProgramStats = new ConcurrentHashMap<>();
        this.chatManager = new ChatManager();
    }

    // Inner class to track global statistics for each program
    public static class ProgramStats {
        private int totalExecutions;
        private int totalCreditCost;

        public ProgramStats() {
            this.totalExecutions = 0;
            this.totalCreditCost = 0;
        }

        public synchronized void addExecution(int creditCost) {
            totalExecutions++;
            totalCreditCost += creditCost;
        }

        public int getTotalExecutions() {
            return totalExecutions;
        }

        public double getAverageCreditCost() {
            return totalExecutions > 0 ? (double) totalCreditCost / totalExecutions : 0.0;
        }
    }

    public static ServerContext getInstance() {
        if (instance == null) {
            synchronized (ServerContext.class) {
                if (instance == null) {
                    instance = new ServerContext();
                }
            }
        }
        return instance;
    }

    // Global program statistics methods
    public void recordProgramExecution(String programName, int creditCost) {
        ProgramStats stats = globalProgramStats.computeIfAbsent(programName, k -> new ProgramStats());
        stats.addExecution(creditCost);
        System.out.println("Recorded execution for program '" + programName + "': cost=" + creditCost +
                         ", total executions=" + stats.getTotalExecutions() +
                         ", avg cost=" + stats.getAverageCreditCost());
    }

    public int getProgramExecutionCount(String programName) {
        ProgramStats stats = globalProgramStats.get(programName);
        return stats != null ? stats.getTotalExecutions() : 0;
    }

    public double getProgramAverageCreditCost(String programName) {
        ProgramStats stats = globalProgramStats.get(programName);
        return stats != null ? stats.getAverageCreditCost() : 0.0;
    }

    // User management methods
    public User getOrCreateUser(String userId) {
        return users.computeIfAbsent(userId, User::new);
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public Map<String, User> getAllUsers() {
        return new ConcurrentHashMap<>(users);
    }

    public boolean hasUser(String userId) {
        return users.containsKey(userId);
    }

    // Program management methods (delegates to User class)
    public void storeUserProgram(String userId, String programName, S_Emulator program) {
        User user = getOrCreateUser(userId);
        user.addProgram(programName, program);
    }

    public S_Emulator getUserProgram(String userId, String programName) {
        User user = getUser(userId);
        return user != null ? user.getProgram(programName) : null;
    }

    public Map<String, S_Emulator> getAllUserPrograms(String userId) {
        User user = getUser(userId);
        return user != null ? user.getAllPrograms() : new ConcurrentHashMap<>();
    }

    public void removeUserProgram(String userId, String programName) {
        User user = getUser(userId);
        if (user != null) {
            user.removeProgram(programName);
        }
    }

    public boolean hasUserProgram(String userId, String programName) {
        User user = getUser(userId);
        return user != null && user.hasProgram(programName);
    }

    // Flattened programs method for backwards compatibility
    public Map<String, S_Emulator> getAllStoredPrograms() {
        Map<String, S_Emulator> allPrograms = new ConcurrentHashMap<>();
        for (Map.Entry<String, User> userEntry : users.entrySet()) {
            String userId = userEntry.getKey();
            User user = userEntry.getValue();
            for (Map.Entry<String, S_Emulator> programEntry : user.getAllPrograms().entrySet()) {
                String programName = programEntry.getKey();
                // Use userId_programName as unique key for the flat map
                allPrograms.put(userId + "_" + programName, programEntry.getValue());
            }
        }
        return allPrograms;
    }

    // Credits management methods
    public int getUserCredits(String userId) {
        User user = getUser(userId);
        return user != null ? user.getCredits() : 0;
    }

    public void setUserCredits(String userId, int credits) {
        User user = getOrCreateUser(userId);
        user.setCredits(credits);
    }

    public boolean deductUserCredits(String userId, int amount) {
        User user = getUser(userId);
        return user != null && user.deductCredits(amount);
    }

    public void addUserCredits(String userId, int amount) {
        User user = getOrCreateUser(userId);
        user.addCredits(amount);
    }

    public boolean userHasEnoughCredits(String userId, int requiredCredits) {
        User user = getUser(userId);
        return user != null && user.hasEnoughCredits(requiredCredits);
    }

    // Statistics methods
    public int getTotalProgramCount() {
        return users.values().stream().mapToInt(User::getProgramCount).sum();
    }

    public int getTotalUserCount() {
        return users.size();
    }

    // Chat management methods
    public ChatManager getChatManager() {
        return chatManager;
    }
}