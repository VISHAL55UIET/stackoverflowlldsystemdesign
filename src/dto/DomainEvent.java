package dto;

import enums.ContentKind;
import enums.EventType;
import enums.VoteType;
import model.User;

/**
 * Immutable event published by services.
 *
 * @param type     what happened
 * @param actor    user who performed the action
 * @param owner    user affected by / interested in the action (e.g. the author of the voted answer)
 * @param refId    id of the question / answer / comment the event is about
 * @param kind     QUESTION or ANSWER when relevant, otherwise null
 * @param voteType UPVOTE / DOWNVOTE for vote events, otherwise null
 * @param message  human readable text, used by notifications
 */
public record DomainEvent(EventType type, User actor, User owner, String refId,
                          ContentKind kind, VoteType voteType, String message) {

    public static DomainEvent of(EventType type, User actor, User owner, String refId, String message) {
        return new DomainEvent(type, actor, owner, refId, null, null, message);
    }

    public static DomainEvent vote(EventType type, User actor, User owner, String refId,
                                   ContentKind kind, VoteType voteType) {
        return new DomainEvent(type, actor, owner, refId, kind, voteType,
                actor.getName() + " " + (type == EventType.VOTE_ADDED ? "gave a " : "removed a ")
                        + voteType + " on your " + kind.name().toLowerCase() + " " + refId);
    }
}
