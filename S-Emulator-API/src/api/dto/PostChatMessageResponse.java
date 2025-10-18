package api.dto;

public class PostChatMessageResponse {
    private long messageId;
    private String timestamp; // ISO-8601 UTC format

    public PostChatMessageResponse() {
    }

    public PostChatMessageResponse(long messageId, String timestamp) {
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
