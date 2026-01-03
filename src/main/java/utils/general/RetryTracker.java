package utils.general;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryTracker {
    private static final ConcurrentHashMap<String, AtomicInteger> retries = new ConcurrentHashMap<>();
    private static final int MAX_RETRIES = 1;

    public static boolean canRetry(String scenarioKey) {
        retries.putIfAbsent(scenarioKey, new AtomicInteger(0));
        return retries.get(scenarioKey).get() < MAX_RETRIES;
    }

    public static int increment(String scenarioKey) {
        retries.putIfAbsent(scenarioKey, new AtomicInteger(0));
        return retries.get(scenarioKey).incrementAndGet();
    }

    public static void clear(String scenarioKey) {
        retries.remove(scenarioKey);
    }  
}