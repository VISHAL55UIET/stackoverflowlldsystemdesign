package dto;

import java.util.List;

/** Input for asking a question. */
public record QuestionRequest(String title, String body, List<String> tags) {
}
