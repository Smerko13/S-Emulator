package util.http;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple cookie manager for OkHttp to maintain session cookies
 */
public class SimpleCookieManager implements CookieJar {
    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();
    private Consumer<String> logConsumer;

    @Override
    public void saveFromResponse(@NotNull HttpUrl url, @NotNull List<Cookie> cookies) {
        cookieStore.put(url.host(), cookies);

        if (logConsumer != null && !cookies.isEmpty()) {
            logConsumer.accept("Saving " + cookies.size() + " cookie(s) from " + url.host());
        }
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());

        if (logConsumer != null && cookies != null && !cookies.isEmpty()) {
            logConsumer.accept("Loading " + cookies.size() + " cookie(s) for " + url.host());
        }

        return cookies != null ? cookies : java.util.Collections.emptyList();
    }

    /**
     * Set a consumer to receive log messages about cookie operations
     *
     * @param logConsumer Consumer that receives log messages
     */
    public void setLogConsumer(Consumer<String> logConsumer) {
        this.logConsumer = logConsumer;
    }
}
