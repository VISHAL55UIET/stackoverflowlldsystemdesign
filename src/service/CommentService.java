package service;

import dto.DomainEvent;
import enums.EventType;
import model.Comment;
import model.Content;
import model.User;

public class CommentService {

    private static final int MAX_LENGTH = 600;

    private final EventPublisher publisher;

    public CommentService(EventPublisher publisher) {
        this.publisher = publisher;
    }

    public Comment addComment(User author, Content target, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Comment text is required");
        }
        if (text.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Comment is limited to " + MAX_LENGTH + " characters");
        }
        Comment comment = new Comment(author, text.trim());
        target.addComment(comment);

        publisher.publish(DomainEvent.of(EventType.COMMENT_ADDED, author, target.getAuthor(), target.getId(),
                author.getName() + " commented on your " + target.kind().name().toLowerCase()
                        + " " + target.getId()));
        return comment;
    }
}
