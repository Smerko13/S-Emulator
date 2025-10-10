package servlets;

import engine.S_Emulator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ServerContext {
    private static volatile ServerContext instance;
    private final Map<String, S_Emulator> userPrograms; // Changed to S_Emulator

    private ServerContext() {
        this.userPrograms = new ConcurrentHashMap<>();
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

    public void storeUserProgram(String userId, S_Emulator program) {
        userPrograms.put(userId, program); // No casting needed
    }

    public S_Emulator getUserProgram(String userId) {
        return userPrograms.get(userId);
    }

    public void removeUserProgram(String userId) {
        userPrograms.remove(userId);
    }

    public boolean hasUserProgram(String userId) {
        return userPrograms.containsKey(userId);
    }

    public Map<String, S_Emulator> getAllStoredPrograms() {
        return new ConcurrentHashMap<>(userPrograms);
    }
}