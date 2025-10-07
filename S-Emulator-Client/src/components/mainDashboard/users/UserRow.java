package components.mainDashboard.users;

import javafx.beans.property.*;

public class UserRow {
    private final StringProperty userName = new SimpleStringProperty("");
    private final IntegerProperty programsUploaded = new SimpleIntegerProperty(0);
    private final IntegerProperty functionsUploaded = new SimpleIntegerProperty(0);
    private final IntegerProperty creditsAvailable  = new SimpleIntegerProperty(0);
    private final IntegerProperty creditsUsed       = new SimpleIntegerProperty(0);
    private final IntegerProperty totalExecutions   = new SimpleIntegerProperty(0);

    public UserRow(String name) { this.userName.set(name); }

    public StringProperty  userNameProperty()        { return userName; }
    public IntegerProperty programsUploadedProperty(){ return programsUploaded; }
    public IntegerProperty functionsUploadedProperty(){ return functionsUploaded; }
    public IntegerProperty creditsAvailableProperty(){ return creditsAvailable; }
    public IntegerProperty creditsUsedProperty()     { return creditsUsed; }
    public IntegerProperty totalExecutionsProperty() { return totalExecutions; }
}
