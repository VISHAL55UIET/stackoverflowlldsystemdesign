package model;

import enums.ContentKind;
import enums.QuestionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** A question asked by a user, with tags, answers, an optional accepted answer and a view counter. */
public class Question extends Content {

    private final String title;
    private final Set<Tag> tags;
    private final List<Answer> answers = new CopyOnWriteArrayList<>();
    private final AtomicInteger views = new AtomicInteger();
    private Answer acceptedAnswer;
    private QuestionStatus status = QuestionStatus.OPEN;

    public Question(User author, String title, String body, Set<Tag> tags) {
        super("Q", author, body);
        this.title = title;
        this.tags = Collections.unmodifiableSet(tags);
    }

    @Override
    public ContentKind kind() {
        return ContentKind.QUESTION;
    }

    public String getTitle() {
        return title;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public int getViews() {
        return views.get();
    }

    public void incrementViews() {
        views.incrementAndGet();
    }

    public synchronized QuestionStatus getStatus() {
        return status;
    }

    public synchronized void close() {
        status = QuestionStatus.CLOSED;
    }

    public synchronized void addAnswer(Answer answer) {
        if (status != QuestionStatus.OPEN) {
            throw new IllegalStateException("Question " + getId() + " is closed and cannot take new answers");
        }
        answers.add(answer);
    }

    /** Accepted answer first, then by score (highest first), then oldest first. */
    public List<Answer> getAnswers() {
        List<Answer> copy = new ArrayList<>(answers);
        copy.sort(Comparator.comparing(Answer::isAccepted).reversed()
                .thenComparing(Comparator.comparingInt(Answer::getScore).reversed())
                .thenComparing(Answer::getCreatedAt));
        return copy;
    }

    public synchronized Answer getAcceptedAnswer() {
        return acceptedAnswer;
    }

    /**
     * Marks the given answer as the accepted one.
     * @return the previously accepted answer, or null if there was none
     */
    public synchronized Answer acceptAnswer(Answer answer) {
        Answer previous = acceptedAnswer;
        if (previous != null) {
            previous.setAccepted(false);
        }
        answer.setAccepted(true);
        acceptedAnswer = answer;
        return previous;
    }

    @Override
    public String toString() {
        return getId() + " \"" + title + "\" by " + getAuthor().getName()
                + " [score=" + getScore() + ", answers=" + answers.size()
                + ", views=" + views.get() + ", " + getStatus() + "]";
    }
}
