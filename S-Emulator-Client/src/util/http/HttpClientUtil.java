package util.http;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class HttpClientUtil {

    private final static SimpleCookieManager simpleCookieManager = new SimpleCookieManager();
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .cookieJar(simpleCookieManager)
                    .followRedirects(false)
                    .build();

    // Store ETags for each URL to support cache-friendly polling
    private static final ConcurrentHashMap<String, String> etagCache = new ConcurrentHashMap<>();

    public static void setCookieManagerLoggingFacility(Consumer<String> logConsumer) {
        simpleCookieManager.setLogData(logConsumer);
    }

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

    public static void runAsync(String finalUrl, Callback callback) {
        Request.Builder requestBuilder = new Request.Builder().url(finalUrl);

        // Add ETag header if we have a cached value
        String cachedETag = etagCache.get(finalUrl);
        if (cachedETag != null) {
            requestBuilder.header("If-None-Match", cachedETag);
        }

        Request request = requestBuilder.build();
        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    public static void runAsyncPost(String finalUrl, String jsonBody, Callback callback) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(body)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    public static void runAsyncMultipartPost(String finalUrl, java.io.File file, Callback callback) {
        MediaType XML = MediaType.get("application/xml");
        RequestBody fileBody = RequestBody.create(file, XML);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    /**
     * Store the ETag from a response for future requests
     */
    public static void cacheETag(String url, String etag) {
        if (etag != null && !etag.isEmpty()) {
            etagCache.put(url, etag);
        }
    }

    /**
     * Clear cached ETag for a URL (useful when data is known to have changed)
     */
    public static void clearETag(String url) {
        etagCache.remove(url);
    }

    /**
     * Clear all cached ETags
     */
    public static void clearAllETags() {
        etagCache.clear();
    }

    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
        etagCache.clear();
    }
}
