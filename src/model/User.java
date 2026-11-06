package model;

/**
 * A registered member of the platform.
 * Displayed reputation starts at 1 and never drops below 1 (same rule as Stack Overflow).
 * Internally the raw total is NOT clamped, so reversing an event (undoing a vote, un-accepting an answer)
 * always restores exactly the previous value instead of drifting upward.
 */
public class User {

    private static final int MIN_REPUTATION = 1;

    private final String id;
    private final String name;
    private final String email;
    private int rawReputation = MIN_REPUTATION;

    public User(String name, String email) {
        this.id = IdGenerator.next("U");
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public synchronized int getReputation() {
        return Math.max(MIN_REPUTATION, rawReputation);
    }

    public synchronized void changeReputation(int delta) {
        rawReputation += delta;
    }

    @Override
    public String toString() {
        return name + "(rep=" + getReputation() + ")";
    }
}
