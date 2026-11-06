package service;

import dto.SearchCriteria;
import model.Question;

import java.util.List;
import java.util.stream.Collectors;

public class AuthorSearchStrategy implements SearchStrategy {

    @Override
    public List<Question> filter(List<Question> questions, SearchCriteria criteria) {
        if (criteria.getAuthorId() == null) {
            return questions;
        }
        return questions.stream()
                .filter(q -> q.getAuthor().getId().equals(criteria.getAuthorId()))
                .collect(Collectors.toList());
    }
}
