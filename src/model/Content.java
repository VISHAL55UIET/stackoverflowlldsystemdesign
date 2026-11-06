package model;

import enums.ContentKind;
import enums.VoteType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for anything that has an author, a body, comments and votes
 * (i.e. Question and Answer). Holds one vote per user: userId -> VoteType.
 * Vote methods are synchronized so the score stays consistent under concurrent voting.
 */
public abstract class Content {

    private final String id;
    private final User author;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final List<Comment> comments = new CopyOnWriteArrayList<>();
    private final Map<String, VoteType> votes = new HashMap<>();
    private volatile String body;

    protected Content(String idPrefix, User author, String body) {
        this.id = IdGenerator.next(idPrefix);
        this.author = author;
        this.body = body;
    }

    public abstract ContentKind kind();

    public String getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public void edit(String newBody) {
        this.body = newBody;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ---------- comments ----------

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(new ArrayList<>(comments));
    }

    // ---------- votes ----------

    public synchronized VoteType getVoteBy(String userId) {
        return votes.get(userId);
    }

    public synchronized void putVote(String userId, VoteType type) {
        votes.put(userId, type);
    }

    public synchronized void removeVote(String userId) {
        votes.remove(userId);
    }

    public synchronized int getScore() {
        int score = 0;
        for (VoteType v : votes.values()) {
            score += v.value();
        }
        return score;
    }
}
