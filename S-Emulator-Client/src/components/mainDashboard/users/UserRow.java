package components.mainDashboard.users;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class UserRow {
    private final SimpleStringProperty userName;
    private final SimpleIntegerProperty programsUploaded;
    private final SimpleIntegerProperty functionsUploaded;
    private final SimpleIntegerProperty creditsAvailable;
    private final SimpleIntegerProperty creditsUsed;
    private final SimpleIntegerProperty totalExecutions;

    public UserRow(String userName) {
        this.userName = new SimpleStringProperty(userName);
        this.programsUploaded = new SimpleIntegerProperty(0);
        this.functionsUploaded = new SimpleIntegerProperty(0);
        this.creditsAvailable = new SimpleIntegerProperty(0);
        this.creditsUsed = new SimpleIntegerProperty(0);
        this.totalExecutions = new SimpleIntegerProperty(0);
    }

    public SimpleStringProperty userNameProperty() {
        return userName;
    }

    public SimpleIntegerProperty programsUploadedProperty() {
        return programsUploaded;
    }

    public SimpleIntegerProperty functionsUploadedProperty() {
        return functionsUploaded;
    }

    public SimpleIntegerProperty creditsAvailableProperty() {
        return creditsAvailable;
    }

    public SimpleIntegerProperty creditsUsedProperty() {
        return creditsUsed;
    }

    public SimpleIntegerProperty totalExecutionsProperty() {
        return totalExecutions;
    }
}

