package service;

import dto.SearchCriteria;
import model.Question;

import java.util.List;
import java.util.stream.Collectors;

/** Keeps questions that carry ALL the requested tags. */
public class TagSearchStrategy implements SearchStrategy {

    @Override
    public List<Question> filter(List<Question> questions, SearchCriteria criteria) {
        if (criteria.getTags().isEmpty()) {
            return questions;
        }
        return questions.stream()
                .filter(q -> q.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet())
                        .containsAll(criteria.getTags()))
                .collect(Collectors.toList());
    }
}
