package service;

import dto.AnswerRequest;
import dto.DomainEvent;
import enums.EventType;
import model.Answer;
import model.Question;
import model.User;
import repository.AnswerRepository;

public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionService questionService;
    private final EventPublisher publisher;

    public AnswerService(AnswerRepository answerRepository, QuestionService questionService,
                         EventPublisher publisher) {
        this.answerRepository = answerRepository;
        this.questionService = questionService;
        this.publisher = publisher;
    }

    public Answer postAnswer(User author, AnswerRequest request) {
        if (request.body() == null || request.body().isBlank()) {
            throw new IllegalArgumentException("Answer body is required");
        }
        Question question = questionService.getQuestion(request.questionId());
        Answer answer = new Answer(author, question, request.body());

        question.addAnswer(answer); // throws if the question is closed
        answerRepository.save(answer);

        publisher.publish(DomainEvent.of(EventType.ANSWER_POSTED, author, question.getAuthor(), answer.getId(),
                author.getName() + " answered your question " + question.getId()));
        return answer;
    }

    public Answer getAnswer(String answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + answerId));
    }

    /**
     * Only the question author can accept. Accepting a different answer silently un-accepts the previous one
     * (and reverses its reputation).
     */
    public void acceptAnswer(User requester, String answerId) {
        Answer answer = getAnswer(answerId);
        Question question = answer.getQuestion();

        if (!question.getAuthor().getId().equals(requester.getId())) {
            throw new SecurityException("Only the question author can accept an answer");
        }

        synchronized (question) {
            if (question.getAcceptedAnswer() == answer) {
                return; // already accepted - idempotent
            }
            Answer previous = question.acceptAnswer(answer);
            if (previous != null) {
                publisher.publish(DomainEvent.of(EventType.ANSWER_ACCEPT_REVOKED, requester, previous.getAuthor(),
                        previous.getId(), "Your answer " + previous.getId() + " is no longer accepted"));
            }
            publisher.publish(DomainEvent.of(EventType.ANSWER_ACCEPTED, requester, answer.getAuthor(), answer.getId(),
                    requester.getName() + " accepted your answer " + answer.getId()));
        }
    }
}
