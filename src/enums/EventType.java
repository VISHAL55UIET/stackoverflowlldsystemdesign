package enums;

/** Domain events published by services and consumed by observers (reputation, notifications). */
public enum EventType {
    QUESTION_POSTED,
    QUESTION_CLOSED,
    ANSWER_POSTED,
    ANSWER_ACCEPTED,
    ANSWER_ACCEPT_REVOKED,
    COMMENT_ADDED,
    VOTE_ADDED,
    VOTE_REMOVED
}
