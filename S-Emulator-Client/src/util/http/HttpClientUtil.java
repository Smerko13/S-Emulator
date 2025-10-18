package util.http;

import okhttp3.*;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * HTTP Client Utility using OkHttp for async requests
 */
public class HttpClientUtil {
    private static final SimpleCookieManager COOKIE_MANAGER = new SimpleCookieManager();

    // ETag cache for conditional requests
    private static final Map<String, String> etagCache = new ConcurrentHashMap<>();

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .followRedirects(true)
            .cookieJar(COOKIE_MANAGER)
            .build();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Set a logging facility for cookie manager operations
     *
     * @param logConsumer Consumer that receives log messages
     */
    public static void setCookieManagerLoggingFacility(Consumer<String> logConsumer) {
        COOKIE_MANAGER.setLogConsumer(logConsumer);
    }

    /**
     * Cache an ETag for a specific URL
     *
     * @param url The URL to cache the ETag for
     * @param etag The ETag value to cache
     */
    public static void cacheETag(String url, String etag) {
        if (url != null && etag != null) {
            etagCache.put(url, etag);
        }
    }

    /**
     * Get cached ETag for a URL
     *
     * @param url The URL to get the cached ETag for
     * @return The cached ETag or null if not found
     */
    public static String getCachedETag(String url) {
        return etagCache.get(url);
    }

    /**
     * Clear cached ETag for a URL
     *
     * @param url The URL to clear the cached ETag for
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

    /**
     * Run an async GET request
     *
     * @param url      The URL to request
     * @param callback The callback to handle response
     */
    public static void runAsync(String url, Callback callback) {
        Request.Builder requestBuilder = new Request.Builder().url(url).get();

        // Add If-None-Match header if we have a cached ETag
        String cachedETag = getCachedETag(url);
        if (cachedETag != null) {
            requestBuilder.header("If-None-Match", cachedETag);
        }

        Request request = requestBuilder.build();
        HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    /**
     * Run an async request with a custom Request object
     *
     * @param request  The request to execute
     * @param callback The callback to handle response
     */
    public static void runAsync(Request request, Callback callback) {
        HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    /**
     * Run an async POST request with JSON body
     *
     * @param url      The URL to request
     * @param jsonBody The JSON body as a string
     * @param callback The callback to handle response
     */
    public static void runAsyncPost(String url, String jsonBody, Callback callback) {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    /**
     * Run an async POST request with multipart form data (file upload)
     *
     * @param url      The URL to request
     * @param file     The file to upload
     * @param callback The callback to handle response
     */
    public static void runAsyncMultipartPost(String url, File file, Callback callback) {
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/xml"));

        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();

        HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    /**
     * Shutdown the HTTP client
     */
    public static void shutdown() {
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
