package service;

import dto.DomainEvent;
import dto.QuestionRequest;
import enums.EventType;
import model.Question;
import model.Tag;
import model.User;
import repository.QuestionRepository;
import repository.TagRepository;

import java.util.LinkedHashSet;
import java.util.Set;

public class QuestionService {

    private static final int MAX_TAGS = 5;
    private static final int CLOSE_MIN_REPUTATION = 500;

    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;
    private final EventPublisher publisher;

    public QuestionService(QuestionRepository questionRepository, TagRepository tagRepository,
                           EventPublisher publisher) {
        this.questionRepository = questionRepository;
        this.tagRepository = tagRepository;
        this.publisher = publisher;
    }

    public Question askQuestion(User author, QuestionRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.body() == null || request.body().isBlank()) {
            throw new IllegalArgumentException("Body is required");
        }
        if (request.tags() == null || request.tags().isEmpty() || request.tags().size() > MAX_TAGS) {
            throw new IllegalArgumentException("A question needs 1 to " + MAX_TAGS + " tags");
        }

        Set<Tag> tags = new LinkedHashSet<>();
        for (String name : request.tags()) {
            tags.add(tagRepository.getOrCreate(name));
        }

        Question question = questionRepository.save(
                new Question(author, request.title().trim(), request.body(), tags));
        publisher.publish(DomainEvent.of(EventType.QUESTION_POSTED, author, null, question.getId(),
                author.getName() + " asked " + question.getId()));
        return question;
    }

    public Question getQuestion(String questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
    }

    /** Opening a question page: returns it and bumps the view counter. */
    public Question viewQuestion(String questionId) {
        Question q = getQuestion(questionId);
        q.incrementViews();
        return q;
    }

    /** The author, or any user with enough reputation (moderator-like), can close a question. */
    public void closeQuestion(User requester, String questionId) {
        Question q = getQuestion(questionId);
        boolean isAuthor = q.getAuthor().getId().equals(requester.getId());
        if (!isAuthor && requester.getReputation() < CLOSE_MIN_REPUTATION) {
            throw new SecurityException(requester.getName() + " is not allowed to close " + questionId);
        }
        q.close();
        publisher.publish(DomainEvent.of(EventType.QUESTION_CLOSED, requester, q.getAuthor(), q.getId(),
                "Your question " + q.getId() + " was closed by " + requester.getName()));
    }
}
