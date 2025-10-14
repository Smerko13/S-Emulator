package api.dto;

public enum Architecture {
    GENERATION_I(5, "Generation I"),
    GENERATION_II(100, "Generation II"),
    GENERATION_III(500, "Generation III"),
    GENERATION_IV(1000, "Generation IV");

    private final int cost;
    private final String displayName;

    Architecture(int cost, String displayName) {
        this.cost = cost;
        this.displayName = displayName;
    }

    public int getCost() {
        return cost;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName + " (" + cost + " credits)";
    }

    public static Architecture fromString(String name) {
        for (Architecture arch : values()) {
            if (arch.name().equals(name) || arch.displayName.equals(name)) {
                return arch;
            }
        }
        return GENERATION_I; // Default fallback
    }
}
