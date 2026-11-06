package service;

import dto.SearchCriteria;
import model.Question;

import java.util.List;
import java.util.stream.Collectors;

/** Case-insensitive match against question title and body. */
public class KeywordSearchStrategy implements SearchStrategy {

    @Override
    public List<Question> filter(List<Question> questions, SearchCriteria criteria) {
        String keyword = criteria.getKeyword();
        if (keyword == null || keyword.isBlank()) {
            return questions;
        }
        String k = keyword.trim().toLowerCase();
        return questions.stream()
                .filter(q -> q.getTitle().toLowerCase().contains(k) || q.getBody().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }
}
