package service;

import dto.SearchCriteria;
import model.Question;

import java.util.List;

/** Strategy pattern: each implementation narrows the result list by one criterion (or does nothing if absent). */
public interface SearchStrategy {

    List<Question> filter(List<Question> questions, SearchCriteria criteria);
}
