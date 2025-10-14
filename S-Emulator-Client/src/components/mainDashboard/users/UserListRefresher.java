package components.mainDashboard.users;

import api.dto.UserSummary;
import util.Constants;
import util.http.HttpClientUtil;
import javafx.beans.property.BooleanProperty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

import static util.Constants.GSON_INSTANCE;

public class UserListRefresher extends TimerTask {

    private final Consumer<String> httpRequestLoggerConsumer;
    private final Consumer<List<UserSummary>> usersListConsumer;
    private int requestNumber;
    private final BooleanProperty shouldUpdate;


    public UserListRefresher(BooleanProperty shouldUpdate, Consumer<String> httpRequestLoggerConsumer, Consumer<List<UserSummary>> usersListConsumer) {
        this.shouldUpdate = shouldUpdate;
        this.httpRequestLoggerConsumer = httpRequestLoggerConsumer;
        this.usersListConsumer = usersListConsumer;
        requestNumber = 0;
    }

    @Override
    public void run() {
        System.out.println("UserListRefresher: run() called, shouldUpdate=" + shouldUpdate.get());
        if (!shouldUpdate.get()) {
            System.out.println("UserListRefresher: shouldUpdate is false, skipping refresh.");
            return;
        }
        final int finalRequestNumber = ++requestNumber;
        System.out.println("UserListRefresher: About to invoke: " + Constants.USERS_LIST + " | Users Request # " + finalRequestNumber);
        httpRequestLoggerConsumer.accept("About to invoke: " + Constants.USERS_LIST + " | Users Request # " + finalRequestNumber);
        HttpClientUtil.runAsync(Constants.USERS_LIST, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("UserListRefresher: HTTP request failed: " + e.getMessage());
                httpRequestLoggerConsumer.accept("Users Request # " + finalRequestNumber + " | Ended with failure...");
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String jsonArrayOfUserSummaries = response.body().string();
                System.out.println("UserListRefresher: HTTP response: " + jsonArrayOfUserSummaries);
                httpRequestLoggerConsumer.accept("Users Request # " + finalRequestNumber + " | Response: " + jsonArrayOfUserSummaries);
                api.dto.UserSummary[] userSummaries = GSON_INSTANCE.fromJson(jsonArrayOfUserSummaries, api.dto.UserSummary[].class);
                System.out.println("UserListRefresher: Parsed " + userSummaries.length + " users: " + Arrays.toString(userSummaries));
                usersListConsumer.accept(Arrays.asList(userSummaries));
                System.out.println("UserListRefresher: usersListConsumer.accept() called.");
            }
        });
    }
}
