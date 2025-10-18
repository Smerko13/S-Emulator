package api.dto;

import java.util.List;

public class ChatMessagesResponseDTO {
    private List<ChatMessageDTO> items;
    private String nextSince; // ISO-8601 timestamp for next poll

    public ChatMessagesResponseDTO() {
    }

    public ChatMessagesResponseDTO(List<ChatMessageDTO> items, String nextSince) {
        this.items = items;
        this.nextSince = nextSince;
    }

    public List<ChatMessageDTO> getItems() {
        return items;
    }

    public void setItems(List<ChatMessageDTO> items) {
        this.items = items;
    }

    public String getNextSince() {
        return nextSince;
    }

    public void setNextSince(String nextSince) {
        this.nextSince = nextSince;
    }
}

