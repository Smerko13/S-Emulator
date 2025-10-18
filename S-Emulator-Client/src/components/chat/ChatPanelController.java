package components.chat;

import api.dto.ChatMessageDTO;
import api.dto.ChatMessagesResponseDTO;
import api.dto.PostChatMessageResponse;
import components.api.ChatHttpClient;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import util.Constants;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller for the Chat Panel component
 * Handles chat message display, sending, and auto-refresh polling
 */
public class ChatPanelController {
    @FXML private VBox chatContainer;
    @FXML private ListView<ChatMessageDisplay> chatListView;
    @FXML private TextArea messageInputArea;
    @FXML private Button sendButton;
    @FXML private Label statusLabel;
    @FXML private VBox newMessagesIndicator; // Changed from Label to VBox

    private String userId;
    private String username;
    private String lastTimestamp;
    private Timer pollTimer;
    private final BooleanProperty autoScrollEnabled = new SimpleBooleanProperty(true);
    private final BooleanProperty sendingMessage = new SimpleBooleanProperty(false);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        // Set up chat list view
        chatListView.setCellFactory(param -> new ChatMessageCell());

        // Bind send button to message input AND sending state
        sendButton.disableProperty().bind(
            messageInputArea.textProperty().isEmpty().or(sendingMessage)
        );

        // Handle scroll detection to disable auto-scroll when user scrolls up
        chatListView.scrollTo(chatListView.getItems().size() - 1);

        // Hide new messages indicator initially
        newMessagesIndicator.setVisible(false);
        newMessagesIndicator.setManaged(false);

        // Set up enter key to send message (Ctrl+Enter for new line)
        messageInputArea.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !event.isControlDown()) {
                event.consume();
                sendMessage();
            }
        });
    }

    /**
     * Start the chat with user credentials
     */
    public void startChat(String userId, String username) {
        this.userId = userId;
        this.username = username;

        // Load initial chat history
        loadChatMessages(null);

        // Start polling for new messages
        startPolling();
    }

    /**
     * Stop the chat polling
     */
    public void stopChat() {
        if (pollTimer != null) {
            pollTimer.cancel();
            pollTimer = null;
        }
    }

    /**
     * Start polling for new messages
     */
    private void startPolling() {
        if (pollTimer != null) {
            pollTimer.cancel();
        }

        pollTimer = new Timer(true);
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loadChatMessages(lastTimestamp);
            }
        }, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    /**
     * Load chat messages from server
     */
    private void loadChatMessages(String since) {
        ChatHttpClient.fetchMessages(since, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error loading messages: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Failed to load messages: " + response.code());
                    });
                    response.close();
                    return;
                }

                String body = response.body() != null ? response.body().string() : "";
                response.close();

                try {
                    ChatMessagesResponseDTO messagesResponse = ChatHttpClient.parseMessagesResponse(body);

                    Platform.runLater(() -> {
                        if (messagesResponse.getItems() != null && !messagesResponse.getItems().isEmpty()) {
                            boolean wasAtBottom = isScrolledToBottom();

                            for (ChatMessageDTO msg : messagesResponse.getItems()) {
                                addMessageToList(msg);
                            }

                            // Auto-scroll if user was at bottom, otherwise show indicator
                            if (wasAtBottom || autoScrollEnabled.get()) {
                                scrollToBottom();
                                hideNewMessagesIndicator();
                            } else {
                                showNewMessagesIndicator();
                            }
                        }

                        // Update last timestamp for next poll
                        if (messagesResponse.getNextSince() != null) {
                            lastTimestamp = messagesResponse.getNextSince();
                        }

                        statusLabel.setText("Connected");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error parsing messages: " + e.getMessage());
                    });
                }
            }
        });
    }

    /**
     * Send a chat message
     */
    @FXML
    private void sendMessage() {
        String text = messageInputArea.getText().trim();
        if (text.isEmpty() || sendingMessage.get()) {
            return;
        }

        // Set sending state
        sendingMessage.set(true);
        statusLabel.setText("Sending...");

        ChatHttpClient.postMessage(userId, text, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error sending message: " + e.getMessage());
                    sendingMessage.set(false);
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                response.close();

                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        messageInputArea.clear();
                        statusLabel.setText("Message sent");
                    } else {
                        // Try to parse error message
                        String errorMsg = body;
                        try {
                            var json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
                            if (json.has("error")) {
                                errorMsg = json.get("error").getAsString();
                            }
                        } catch (Exception ignore) {}

                        statusLabel.setText("Error: " + errorMsg);
                    }
                    sendingMessage.set(false);
                });
            }
        });
    }

    /**
     * Add a message to the chat list
     */
    private void addMessageToList(ChatMessageDTO msg) {
        // Check if message already exists
        for (ChatMessageDisplay existing : chatListView.getItems()) {
            if (existing.messageId == msg.getMessageId()) {
                return; // Already displayed
            }
        }

        ChatMessageDisplay display = new ChatMessageDisplay(
            msg.getMessageId(),
            msg.getUserId(),
            msg.getUsername(),
            msg.getText(),
            msg.getTimestamp()
        );

        chatListView.getItems().add(display);
    }

    /**
     * Check if list view is scrolled to bottom
     */
    private boolean isScrolledToBottom() {
        if (chatListView.getItems().isEmpty()) {
            return true;
        }
        // Simple heuristic: if we can see the last item
        int lastIndex = chatListView.getItems().size() - 1;
        return chatListView.getItems().size() < 10; // Auto-scroll if less than 10 messages
    }

    /**
     * Scroll to the bottom of the chat
     */
    private void scrollToBottom() {
        if (!chatListView.getItems().isEmpty()) {
            chatListView.scrollTo(chatListView.getItems().size() - 1);
        }
    }

    /**
     * Show new messages indicator
     */
    private void showNewMessagesIndicator() {
        newMessagesIndicator.setVisible(true);
        newMessagesIndicator.setManaged(true);
    }

    /**
     * Hide new messages indicator
     */
    private void hideNewMessagesIndicator() {
        newMessagesIndicator.setVisible(false);
        newMessagesIndicator.setManaged(false);
    }

    /**
     * Scroll to bottom when indicator is clicked
     */
    @FXML
    private void onNewMessagesClicked() {
        scrollToBottom();
        hideNewMessagesIndicator();
    }

    /**
     * Inner class to hold chat message display data
     */
    private static class ChatMessageDisplay {
        final long messageId;
        final String userId;
        final String username;
        final String text;
        final String timestamp;
        final String formattedTime;

        ChatMessageDisplay(long messageId, String userId, String username, String text, String timestamp) {
            this.messageId = messageId;
            this.userId = userId;
            this.username = username;
            this.text = text;
            this.timestamp = timestamp;

            // Format timestamp for display
            String tempFormattedTime;
            try {
                Instant instant = Instant.parse(timestamp);
                tempFormattedTime = instant.atZone(ZoneId.systemDefault()).format(TIME_FORMATTER);
            } catch (Exception e) {
                tempFormattedTime = "";
            }
            this.formattedTime = tempFormattedTime;
        }
    }

    /**
     * Custom cell for displaying chat messages
     */
    private static class ChatMessageCell extends ListCell<ChatMessageDisplay> {
        @Override
        protected void updateItem(ChatMessageDisplay item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                String displayText = String.format("[%s] %s: %s",
                    item.formattedTime,
                    item.username,
                    item.text);
                setText(displayText);
            }
        }
    }
}
