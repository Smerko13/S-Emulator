package engine.chat;

import java.time.Instant;

public class SingleChatEntry {
    private final long messageId;
    private final String userId;
    private final String chatString;
    private final String username;
    private final long time;
    private final String timestamp; // ISO-8601 UTC format

    public SingleChatEntry(long messageId, String userId, String chatString, String username) {
        this.messageId = messageId;
        this.userId = userId;
        this.chatString = chatString;
        this.username = username;
        this.time = System.currentTimeMillis();
        this.timestamp = Instant.ofEpochMilli(time).toString();
    }

    public long getMessageId() {
        return messageId;
    }

    public String getUserId() {
        return userId;
    }

    public String getChatString() {
        return chatString;
    }

    public long getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return (username != null ? username + ": " : "") + chatString;
    }
}
