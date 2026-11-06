package service;

import dto.DomainEvent;
import enums.EventType;
import enums.VoteType;
import model.Content;
import model.User;

/**
 * Handles voting on questions and answers.
 *
 * Rules:
 *  - you cannot vote on your own post
 *  - one vote per user per post
 *  - voting the same way again undoes the vote
 *  - voting the opposite way switches the vote (old vote is removed, new one added)
 *
 * The whole read-modify-write runs under the content's monitor, so concurrent voters can never
 * double count and reputation events are always published in a consistent order.
 */
public class VoteService {

    private final EventPublisher publisher;

    public VoteService(EventPublisher publisher) {
        this.publisher = publisher;
    }

    /** @return the new score of the content after the vote */
    public int vote(User voter, Content content, VoteType type) {
        if (content.getAuthor().getId().equals(voter.getId())) {
            throw new IllegalArgumentException("You cannot vote on your own " + content.kind().name().toLowerCase());
        }

        synchronized (content) {
            VoteType previous = content.getVoteBy(voter.getId());

            if (previous == type) {
                content.removeVote(voter.getId());
                publish(EventType.VOTE_REMOVED, voter, content, type);
            } else {
                if (previous != null) {
                    content.removeVote(voter.getId());
                    publish(EventType.VOTE_REMOVED, voter, content, previous);
                }
                content.putVote(voter.getId(), type);
                publish(EventType.VOTE_ADDED, voter, content, type);
            }
            return content.getScore();
        }
    }

    private void publish(EventType eventType, User voter, Content content, VoteType type) {
        publisher.publish(DomainEvent.vote(eventType, voter, content.getAuthor(),
                content.getId(), content.kind(), type));
    }
}
