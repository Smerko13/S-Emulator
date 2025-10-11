package components.shared;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Singleton class to manage user session data across different dashboard components.
 * This ensures user information (name, credits) persists when switching between
 * main dashboard and execution dashboard.
 */
public class UserSession {
    private static UserSession instance;

    private final StringProperty userName;
    private final IntegerProperty credits;

    private UserSession() {
        userName = new SimpleStringProperty("Guest");
        credits = new SimpleIntegerProperty(0);
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Username methods
    public String getUserName() {
        return userName.get();
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public StringProperty userNameProperty() {
        return userName;
    }

    // Credits methods
    public int getCredits() {
        return credits.get();
    }

    public void setCredits(int credits) {
        this.credits.set(credits);
    }

    public IntegerProperty creditsProperty() {
        return credits;
    }

    // Utility method to clear session data (e.g., on logout)
    public void clearSession() {
        userName.set("Guest");
        credits.set(0);
    }
}
