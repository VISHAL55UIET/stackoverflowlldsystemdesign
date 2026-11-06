package service;

import dto.DomainEvent;
import enums.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer that keeps a per-user inbox. Users are never notified about their own actions.
 * In production this would hand off to email / push / WebSocket.
 */
public class NotificationService implements DomainEventListener {

    private final Map<String, List<String>> inbox = new ConcurrentHashMap<>();

    public void register(EventPublisher publisher) {
        publisher.subscribe(EventType.ANSWER_POSTED, this);
        publisher.subscribe(EventType.COMMENT_ADDED, this);
        publisher.subscribe(EventType.ANSWER_ACCEPTED, this);
        publisher.subscribe(EventType.ANSWER_ACCEPT_REVOKED, this);
        publisher.subscribe(EventType.QUESTION_CLOSED, this);
        publisher.subscribe(EventType.VOTE_ADDED, this);
    }

    @Override
    public void onEvent(DomainEvent e) {
        if (e.owner() == null || e.owner().getId().equals(e.actor().getId())) {
            return;
        }
        inbox.computeIfAbsent(e.owner().getId(), k -> new CopyOnWriteArrayList<>()).add(e.message());
    }

    public List<String> getNotifications(String userId) {
        return new ArrayList<>(inbox.getOrDefault(userId, List.of()));
    }
}
