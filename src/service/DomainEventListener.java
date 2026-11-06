package service;

import dto.DomainEvent;

/** Observer contract: anything interested in domain events implements this. */
public interface DomainEventListener {

    void onEvent(DomainEvent event);
}
