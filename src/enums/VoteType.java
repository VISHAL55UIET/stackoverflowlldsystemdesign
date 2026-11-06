package enums;

/** Direction of a vote cast on a question or an answer. */
public enum VoteType {
    UPVOTE(1),
    DOWNVOTE(-1);

    private final int value;

    VoteType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
