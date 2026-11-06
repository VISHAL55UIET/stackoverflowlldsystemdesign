package repository;

import model.Answer;

import java.util.List;
import java.util.stream.Collectors;

public class AnswerRepository extends InMemoryRepository<Answer> {

    public AnswerRepository() {
        super(Answer::getId);
    }

    public List<Answer> findByQuestionId(String questionId) {
        return findAll().stream()
                .filter(a -> a.getQuestion().getId().equals(questionId))
                .collect(Collectors.toList());
    }
}
