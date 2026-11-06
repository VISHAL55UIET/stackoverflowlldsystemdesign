package model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Thread-safe, prefix-based id generator: Q-1, Q-2, A-1, C-1, U-1 ... */
public final class IdGenerator {

    private static final Map<String, AtomicLong> COUNTERS = new ConcurrentHashMap<>();

    private IdGenerator() {
    }

    public static String next(String prefix) {
        return prefix + "-" + COUNTERS.computeIfAbsent(prefix, k -> new AtomicLong()).incrementAndGet();
    }
}
