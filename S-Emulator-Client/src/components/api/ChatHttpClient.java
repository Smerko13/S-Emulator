package components.api;

import api.dto.ChatMessagesResponseDTO;
import api.dto.PostChatMessageRequest;
import api.dto.PostChatMessageResponse;
import com.google.gson.Gson;
import okhttp3.*;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;

/**
 * HTTP client utility for chat functionality
 */
public class ChatHttpClient {
    private static final Gson gson = Constants.GSON_INSTANCE;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Fetch chat messages since a specific timestamp
     * @param since ISO-8601 timestamp (null or empty for all messages)
     * @param callback Callback to handle the response
     */
    public static void fetchMessages(String since, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.CHAT_MESSAGES).newBuilder();

        if (since != null && !since.isEmpty()) {
            urlBuilder.addQueryParameter("since", since);
        }

        String url = urlBuilder.build().toString();
        HttpClientUtil.runAsync(url, callback);
    }

    /**
     * Post a new chat message
     * @param userId The user ID
     * @param text The message text
     * @param callback Callback to handle the response
     */
    public static void postMessage(String userId, String text, Callback callback) {
        PostChatMessageRequest request = new PostChatMessageRequest(userId, text);
        String jsonBody = gson.toJson(request);

        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request httpRequest = new Request.Builder()
                .url(Constants.CHAT_MESSAGES)
                .post(body)
                .build();

        HttpClientUtil.runAsync(httpRequest, callback);
    }

    /**
     * Parse chat messages response from JSON
     * @param json JSON response string
     * @return ChatMessagesResponseDTO
     */
    public static ChatMessagesResponseDTO parseMessagesResponse(String json) {
        return gson.fromJson(json, ChatMessagesResponseDTO.class);
    }

    /**
     * Parse post message response from JSON
     * @param json JSON response string
     * @return PostChatMessageResponse
     */
    public static PostChatMessageResponse parsePostResponse(String json) {
        return gson.fromJson(json, PostChatMessageResponse.class);
    }
}

