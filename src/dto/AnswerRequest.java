package dto;

/** Input for answering a question. */
public record AnswerRequest(String questionId, String body) {
}
