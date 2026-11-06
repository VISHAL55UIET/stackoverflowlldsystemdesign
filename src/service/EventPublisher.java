package service;

import dto.DomainEvent;
import enums.EventType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** Observer pattern subject: services publish events here; listeners subscribe per event type. */
public class EventPublisher {

    private final Map<EventType, List<DomainEventListener>> listeners = new ConcurrentHashMap<>();

    public void subscribe(EventType type, DomainEventListener listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void publish(DomainEvent event) {
        for (DomainEventListener l : listeners.getOrDefault(event.type(), List.of())) {
            l.onEvent(event);
        }
    }
}
