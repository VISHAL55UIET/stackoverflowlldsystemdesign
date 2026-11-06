package repository;

import model.Question;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionRepository extends InMemoryRepository<Question> {

    public QuestionRepository() {
        super(Question::getId);
    }

    public List<Question> findByTag(String tagName) {
        String t = tagName.trim().toLowerCase();
        return findAll().stream()
                .filter(q -> q.getTags().stream().anyMatch(tag -> tag.getName().equals(t)))
                .collect(Collectors.toList());
    }

    public List<Question> findByAuthor(String authorId) {
        return findAll().stream()
                .filter(q -> q.getAuthor().getId().equals(authorId))
                .collect(Collectors.toList());
    }
}
