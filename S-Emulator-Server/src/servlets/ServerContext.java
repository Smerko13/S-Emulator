package servlets;

import engine.S_Emulator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ServerContext {
    private static volatile ServerContext instance;
    private final Map<String, Map<String, S_Emulator>> userPrograms; // userId -> (programName -> S_Emulator)

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

    public void storeUserProgram(String userId, String programName, S_Emulator program) {
        userPrograms.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(programName, program);
    }

    public S_Emulator getUserProgram(String userId, String programName) {
        Map<String, S_Emulator> programs = userPrograms.get(userId);
        return programs != null ? programs.get(programName) : null;
    }

    public Map<String, S_Emulator> getAllUserPrograms(String userId) {
        Map<String, S_Emulator> programs = userPrograms.get(userId);
        return programs != null ? new ConcurrentHashMap<>(programs) : new ConcurrentHashMap<>();
    }

    public void removeUserProgram(String userId, String programName) {
        Map<String, S_Emulator> programs = userPrograms.get(userId);
        if (programs != null) {
            programs.remove(programName);
            if (programs.isEmpty()) {
                userPrograms.remove(userId);
            }
        }
    }

    public boolean hasUserProgram(String userId, String programName) {
        Map<String, S_Emulator> programs = userPrograms.get(userId);
        return programs != null && programs.containsKey(programName);
    }

    public Map<String, S_Emulator> getAllStoredPrograms() {
        Map<String, S_Emulator> allPrograms = new ConcurrentHashMap<>();
        for (Map.Entry<String, Map<String, S_Emulator>> userEntry : userPrograms.entrySet()) {
            String userId = userEntry.getKey();
            for (Map.Entry<String, S_Emulator> programEntry : userEntry.getValue().entrySet()) {
                String programName = programEntry.getKey();
                // Use userId_programName as unique key for the flat map
                allPrograms.put(userId + "_" + programName, programEntry.getValue());
            }
        }
        return allPrograms;
    }

    public int getTotalProgramCount() {
        return userPrograms.values().stream().mapToInt(Map::size).sum();
    }
}