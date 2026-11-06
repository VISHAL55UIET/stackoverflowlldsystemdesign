package service;

import dto.DomainEvent;
import enums.ContentKind;
import enums.EventType;
import enums.VoteType;

/**
 * Observer that turns domain events into reputation changes.
 *
 * Rules (modelled on Stack Overflow):
 *   question upvote   : author +5
 *   answer upvote     : author +10
 *   any downvote      : author -2
 *   answer downvote   : voter  -1
 *   answer accepted   : answer author +15, question author (accepter) +2
 * Removing a vote / un-accepting applies the exact reverse.
 */
public class ReputationService implements DomainEventListener {

    static final int QUESTION_UPVOTE = 5;
    static final int ANSWER_UPVOTE = 10;
    static final int DOWNVOTE_PENALTY = -2;
    static final int ANSWER_DOWNVOTE_VOTER_COST = -1;
    static final int ANSWER_ACCEPTED = 15;
    static final int ACCEPTER_BONUS = 2;

    public void register(EventPublisher publisher) {
        publisher.subscribe(EventType.VOTE_ADDED, this);
        publisher.subscribe(EventType.VOTE_REMOVED, this);
        publisher.subscribe(EventType.ANSWER_ACCEPTED, this);
        publisher.subscribe(EventType.ANSWER_ACCEPT_REVOKED, this);
    }

    @Override
    public void onEvent(DomainEvent e) {
        switch (e.type()) {
            case VOTE_ADDED -> applyVote(e, 1);
            case VOTE_REMOVED -> applyVote(e, -1);
            case ANSWER_ACCEPTED -> applyAccept(e, 1);
            case ANSWER_ACCEPT_REVOKED -> applyAccept(e, -1);
            default -> {
            }
        }
    }

    private void applyVote(DomainEvent e, int direction) {
        int ownerPoints;
        if (e.voteType() == VoteType.DOWNVOTE) {
            ownerPoints = DOWNVOTE_PENALTY;
        } else {
            ownerPoints = e.kind() == ContentKind.QUESTION ? QUESTION_UPVOTE : ANSWER_UPVOTE;
        }
        e.owner().changeReputation(direction * ownerPoints);

        if (e.kind() == ContentKind.ANSWER && e.voteType() == VoteType.DOWNVOTE) {
            e.actor().changeReputation(direction * ANSWER_DOWNVOTE_VOTER_COST);
        }
    }

    private void applyAccept(DomainEvent e, int direction) {
        // Accepting your own answer earns nothing.
        if (e.owner().getId().equals(e.actor().getId())) {
            return;
        }
        e.owner().changeReputation(direction * ANSWER_ACCEPTED);
        e.actor().changeReputation(direction * ACCEPTER_BONUS);
    }
}
