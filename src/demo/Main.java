package demo;

import dto.AnswerRequest;
import dto.CommentRequest;
import dto.QuestionRequest;
import dto.SearchCriteria;
import enums.SortBy;
import enums.VoteType;
import model.Answer;
import model.Question;
import model.User;
import service.StackOverflowService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        StackOverflowService app = new StackOverflowService();

        section("1. Register users");
        User alice = app.registerUser("Alice", "alice@example.com");
        User bob = app.registerUser("Bob", "bob@example.com");
        User carol = app.registerUser("Carol", "carol@example.com");
        User dave = app.registerUser("Dave", "dave@example.com");
        System.out.println(alice + ", " + bob + ", " + carol + ", " + dave);
        expectFailure("duplicate email", () -> app.registerUser("Alice2", "alice@example.com"));

        section("2. Ask questions");
        Question q1 = app.askQuestion(alice.getId(), new QuestionRequest(
                "How do I reverse a linked list in Java?",
                "I have a singly linked list and need to reverse it in place.",
                List.of("java", "linked-list", "algorithms")));
        Question q2 = app.askQuestion(dave.getId(), new QuestionRequest(
                "Difference between HashMap and ConcurrentHashMap?",
                "When should I prefer one over the other in multithreaded code?",
                List.of("java", "concurrency")));
        System.out.println(q1);
        System.out.println(q2);
        expectFailure("question with no tags", () -> app.askQuestion(alice.getId(),
                new QuestionRequest("Title", "Body", List.of())));

        section("3. Answer questions");
        Answer a1 = app.answerQuestion(bob.getId(), new AnswerRequest(q1.getId(),
                "Iterate with prev/curr/next pointers and flip each link. O(n) time, O(1) space."));
        Answer a2 = app.answerQuestion(carol.getId(), new AnswerRequest(q1.getId(),
                "Use recursion: reverse the rest, then point head.next.next to head."));
        System.out.println(a1);
        System.out.println(a2);

        section("4. Comments");
        app.comment(alice.getId(), new CommentRequest(a1.getId(), "Thanks, does this handle an empty list?"));
        app.comment(bob.getId(), new CommentRequest(a1.getId(), "Yes - the loop simply never runs."));
        app.comment(dave.getId(), new CommentRequest(q1.getId(), "Is it a doubly linked list?"));
        a1.getComments().forEach(c -> System.out.println("  on " + a1.getId() + " -> " + c));

        section("5. Voting");
        System.out.println("Carol upvotes question Q1      -> score " + app.vote(carol.getId(), q1.getId(), VoteType.UPVOTE));
        System.out.println("Dave upvotes Bob's answer      -> score " + app.vote(dave.getId(), a1.getId(), VoteType.UPVOTE));
        System.out.println("Alice upvotes Bob's answer     -> score " + app.vote(alice.getId(), a1.getId(), VoteType.UPVOTE));
        System.out.println("Dave downvotes Carol's answer  -> score " + app.vote(dave.getId(), a2.getId(), VoteType.DOWNVOTE));
        System.out.println("Dave switches to upvote        -> score " + app.vote(dave.getId(), a2.getId(), VoteType.UPVOTE));
        System.out.println("Dave upvotes again (undo)      -> score " + app.vote(dave.getId(), a2.getId(), VoteType.UPVOTE));
        expectFailure("voting on own post", () -> app.vote(bob.getId(), a1.getId(), VoteType.UPVOTE));
        printReputation("After voting", alice, bob, carol, dave);

        section("6. Accept answer");
        expectFailure("non-author accepting", () -> app.acceptAnswer(carol.getId(), a2.getId()));
        app.acceptAnswer(alice.getId(), a1.getId());
        System.out.println("Alice accepted " + a1.getId() + " -> " + a1);
        app.acceptAnswer(alice.getId(), a2.getId());
        System.out.println("Alice switched to " + a2.getId() + " -> " + a2 + " | " + a1);
        app.acceptAnswer(alice.getId(), a1.getId());
        System.out.println("Alice switched back to " + a1.getId());
        System.out.println("Answers in display order: " + q1.getAnswers());
        printReputation("After accepting", alice, bob, carol, dave);

        section("7. Search");
        app.viewQuestion(q2.getId());
        app.viewQuestion(q2.getId());
        app.viewQuestion(q1.getId());
        printResults("keyword 'linked list'", app.search(SearchCriteria.builder().keyword("linked list").build()));
        printResults("tag 'java', most viewed", app.search(
                SearchCriteria.builder().tag("java").sortBy(SortBy.MOST_VIEWED).build()));
        printResults("tags java + concurrency", app.search(
                SearchCriteria.builder().tag("java").tag("concurrency").build()));
        printResults("by author Alice, most voted", app.search(
                SearchCriteria.builder().authorId(alice.getId()).sortBy(SortBy.MOST_VOTED).build()));

        section("8. Close question");
        app.closeQuestion(alice.getId(), q1.getId());
        expectFailure("answer on closed question", () -> app.answerQuestion(dave.getId(),
                new AnswerRequest(q1.getId(), "Late answer")));
        expectFailure("low-rep user closing someone else's question", () -> app.closeQuestion(carol.getId(), q2.getId()));

        section("9. Notifications");
        for (User u : List.of(alice, bob, carol, dave)) {
            System.out.println(u.getName() + ":");
            app.getNotifications(u.getId()).forEach(n -> System.out.println("   - " + n));
        }

        section("10. Concurrency: 200 users upvote the same answer at once");
        concurrencyTest(app, q2);
    }

    private static void concurrencyTest(StackOverflowService app, Question question) throws Exception {
        User author = app.registerUser("Eve", "eve@example.com");
        Answer answer = app.answerQuestion(author.getId(),
                new AnswerRequest(question.getId(), "ConcurrentHashMap uses fine-grained locking / CAS."));
        int voters = 200;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < voters; i++) {
            users.add(app.registerUser("voter" + i, "voter" + i + "@example.com"));
        }

        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(voters);
        for (User u : users) {
            pool.submit(() -> {
                try {
                    start.await();
                    app.vote(u.getId(), answer.getId(), VoteType.UPVOTE);
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdown();

        int expectedRep = 1 + voters * 10;
        System.out.println("Score      : " + answer.getScore() + " (expected " + voters + ")");
        System.out.println("Eve's rep  : " + author.getReputation() + " (expected " + expectedRep + ")");
        if (answer.getScore() != voters || author.getReputation() != expectedRep) {
            throw new AssertionError("Concurrent voting produced inconsistent state!");
        }
        System.out.println("Concurrency check PASSED");
    }

    // ---------- tiny helpers for readable output ----------

    private static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    private static void printReputation(String label, User... users) {
        StringBuilder sb = new StringBuilder(label + " -> ");
        for (User u : users) {
            sb.append(u.getName()).append('=').append(u.getReputation()).append("  ");
        }
        System.out.println(sb);
    }

    private static void printResults(String label, List<Question> results) {
        System.out.println("Search [" + label + "] -> " + results.size() + " result(s)");
        results.forEach(q -> System.out.println("   " + q));
    }

    private static void expectFailure(String what, Runnable action) {
        try {
            action.run();
            System.out.println("UNEXPECTED: '" + what + "' was allowed");
        } catch (RuntimeException e) {
            System.out.println("Rejected (" + what + "): " + e.getMessage());
        }
    }
}
