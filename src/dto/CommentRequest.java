package dto;

/** Input for commenting. contentId is a question id (Q-..) or an answer id (A-..). */
public record CommentRequest(String contentId, String text) {
}
