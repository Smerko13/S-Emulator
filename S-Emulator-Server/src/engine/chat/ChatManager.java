package engine.chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/*
This class is thread safe for adding/fetching chat messages with rate limiting support
 */
public class ChatManager {

    private final List<SingleChatEntry> chatDataList;
    private final AtomicLong messageIdCounter;
    private final Map<String, RateLimiter> userRateLimiters;

    // Rate limiting: max 5 messages per second per user
    private static final int MAX_MESSAGES_PER_SECOND = 5;
    private static final long RATE_LIMIT_WINDOW_MS = 1000;

    public ChatManager() {
        chatDataList = new ArrayList<>();
        messageIdCounter = new AtomicLong(0);
        userRateLimiters = new ConcurrentHashMap<>();
    }

    public synchronized SingleChatEntry addChatMessage(String userId, String chatString, String username) {
        // Check rate limit
        RateLimiter limiter = userRateLimiters.computeIfAbsent(userId, k -> new RateLimiter());
        if (!limiter.allowMessage()) {
            throw new IllegalStateException("Rate limit exceeded. Maximum " + MAX_MESSAGES_PER_SECOND + " messages per second.");
        }

        long messageId = messageIdCounter.incrementAndGet();
        SingleChatEntry entry = new SingleChatEntry(messageId, userId, chatString, username);
        chatDataList.add(entry);
        return entry;
    }

    // Legacy method for backward compatibility
    public synchronized void addChatString(String chatString, String username) {
        long messageId = messageIdCounter.incrementAndGet();
        chatDataList.add(new SingleChatEntry(messageId, "system", chatString, username));
    }

    public synchronized List<SingleChatEntry> getChatEntries(int fromIndex) {
        if (fromIndex < 0 || fromIndex > chatDataList.size()) {
            fromIndex = 0;
        }
        return new ArrayList<>(chatDataList.subList(fromIndex, chatDataList.size()));
    }

    // Get messages since a specific timestamp
    public synchronized List<SingleChatEntry> getChatEntriesSince(String sinceTimestamp) {
        if (sinceTimestamp == null || sinceTimestamp.isEmpty()) {
            return new ArrayList<>(chatDataList);
        }

        try {
            Instant sinceInstant = Instant.parse(sinceTimestamp);
            long sinceMillis = sinceInstant.toEpochMilli();

            List<SingleChatEntry> result = new ArrayList<>();
            for (SingleChatEntry entry : chatDataList) {
                if (entry.getTime() > sinceMillis) {
                    result.add(entry);
                }
            }
            return result;
        } catch (Exception e) {
            // If timestamp parsing fails, return all messages
            return new ArrayList<>(chatDataList);
        }
    }

    public int getVersion() {
        return chatDataList.size();
    }

    // Inner class for rate limiting
    private static class RateLimiter {
        private final List<Long> timestamps = new ArrayList<>();

        public synchronized boolean allowMessage() {
            long now = System.currentTimeMillis();
            // Remove timestamps outside the window
            timestamps.removeIf(ts -> now - ts > RATE_LIMIT_WINDOW_MS);

            if (timestamps.size() >= MAX_MESSAGES_PER_SECOND) {
                return false;
            }

            timestamps.add(now);
            return true;
        }
    }
}
