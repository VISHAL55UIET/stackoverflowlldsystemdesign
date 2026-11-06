package service;

import dto.SearchCriteria;
import enums.SortBy;
import model.Question;
import repository.QuestionRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Applies every registered SearchStrategy in sequence (AND semantics), then sorts. */
public class SearchService {

    private final QuestionRepository questionRepository;
    private final List<SearchStrategy> strategies;

    public SearchService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
        this.strategies = List.of(
                new KeywordSearchStrategy(),
                new TagSearchStrategy(),
                new AuthorSearchStrategy());
    }

    public List<Question> search(SearchCriteria criteria) {
        List<Question> result = new ArrayList<>(questionRepository.findAll());
        for (SearchStrategy strategy : strategies) {
            result = new ArrayList<>(strategy.filter(result, criteria));
        }
        result.sort(comparatorFor(criteria.getSortBy()));
        return result;
    }

    private Comparator<Question> comparatorFor(SortBy sortBy) {
        return switch (sortBy) {
            case NEWEST -> Comparator.comparing(Question::getCreatedAt).reversed();
            case MOST_VOTED -> Comparator.comparingInt(Question::getScore).reversed();
            case MOST_VIEWED -> Comparator.comparingInt(Question::getViews).reversed();
        };
    }
}
