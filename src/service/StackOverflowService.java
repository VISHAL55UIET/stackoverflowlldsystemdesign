package service;

import dto.AnswerRequest;
import dto.CommentRequest;
import dto.QuestionRequest;
import dto.SearchCriteria;
import enums.VoteType;
import model.Answer;
import model.Comment;
import model.Content;
import model.Question;
import model.User;
import repository.AnswerRepository;
import repository.QuestionRepository;
import repository.TagRepository;
import repository.UserRepository;

import java.util.List;

/**
 * Facade: the single entry point clients (UI / REST controllers / demo) talk to.
 * It wires all repositories, services and observers together and resolves ids to entities.
 */
public class StackOverflowService {

    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;
    private final VoteService voteService;
    private final SearchService searchService;
    private final NotificationService notificationService;

    public StackOverflowService() {
        UserRepository userRepository = new UserRepository();
        QuestionRepository questionRepository = new QuestionRepository();
        AnswerRepository answerRepository = new AnswerRepository();
        TagRepository tagRepository = new TagRepository();

        EventPublisher publisher = new EventPublisher();
        new ReputationService().register(publisher);
        this.notificationService = new NotificationService();
        this.notificationService.register(publisher);

        this.userService = new UserService(userRepository);
        this.questionService = new QuestionService(questionRepository, tagRepository, publisher);
        this.answerService = new AnswerService(answerRepository, questionService, publisher);
        this.commentService = new CommentService(publisher);
        this.voteService = new VoteService(publisher);
        this.searchService = new SearchService(questionRepository);
    }

    // ---------- users ----------

    public User registerUser(String name, String email) {
        return userService.register(name, email);
    }

    public User getUser(String userId) {
        return userService.getUser(userId);
    }

    // ---------- questions ----------

    public Question askQuestion(String userId, QuestionRequest request) {
        return questionService.askQuestion(userService.getUser(userId), request);
    }

    public Question viewQuestion(String questionId) {
        return questionService.viewQuestion(questionId);
    }

    public void closeQuestion(String userId, String questionId) {
        questionService.closeQuestion(userService.getUser(userId), questionId);
    }

    // ---------- answers ----------

    public Answer answerQuestion(String userId, AnswerRequest request) {
        return answerService.postAnswer(userService.getUser(userId), request);
    }

    public void acceptAnswer(String userId, String answerId) {
        answerService.acceptAnswer(userService.getUser(userId), answerId);
    }

    // ---------- comments ----------

    public Comment comment(String userId, CommentRequest request) {
        return commentService.addComment(userService.getUser(userId), findContent(request.contentId()),
                request.text());
    }

    // ---------- votes ----------

    public int vote(String userId, String contentId, VoteType type) {
        return voteService.vote(userService.getUser(userId), findContent(contentId), type);
    }

    // ---------- search / notifications ----------

    public List<Question> search(SearchCriteria criteria) {
        return searchService.search(criteria);
    }

    public List<String> getNotifications(String userId) {
        return notificationService.getNotifications(userId);
    }

    // ---------- helpers ----------

    /** Ids are prefixed: Q-n for questions, A-n for answers. */
    private Content findContent(String contentId) {
        if (contentId.startsWith("Q-")) {
            return questionService.getQuestion(contentId);
        }
        if (contentId.startsWith("A-")) {
            return answerService.getAnswer(contentId);
        }
        throw new IllegalArgumentException("Unknown content id: " + contentId);
    }
}
