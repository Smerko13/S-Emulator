package api.dto;

public class ChatMessageDTO {
    private long messageId;
    private String userId;
    private String username;
    private String text;
    private String timestamp; // ISO-8601 UTC format

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(long messageId, String userId, String username, String text, String timestamp) {
        this.messageId = messageId;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

