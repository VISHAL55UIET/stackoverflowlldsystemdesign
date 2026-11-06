package model;

import enums.ContentKind;

/** An answer posted against a question. At most one answer per question can be accepted. */
public class Answer extends Content {

    private final Question question;
    private volatile boolean accepted;

    public Answer(User author, Question question, String body) {
        super("A", author, body);
        this.question = question;
    }

    @Override
    public ContentKind kind() {
        return ContentKind.ANSWER;
    }

    public Question getQuestion() {
        return question;
    }

    public boolean isAccepted() {
        return accepted;
    }

    void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    @Override
    public String toString() {
        return getId() + " by " + getAuthor().getName() + " [score=" + getScore()
                + (accepted ? ", ACCEPTED" : "") + "]";
    }
}
