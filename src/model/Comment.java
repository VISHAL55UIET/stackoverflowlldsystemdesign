package model;

import java.time.LocalDateTime;

/** A short remark attached to a question or an answer. Comments are not votable. */
public class Comment {

    private final String id;
    private final User author;
    private final String text;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Comment(User author, String text) {
        this.id = IdGenerator.next("C");
        this.author = author;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return author.getName() + ": " + text;
    }
}
