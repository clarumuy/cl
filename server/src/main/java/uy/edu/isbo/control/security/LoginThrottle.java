package uy.edu.isbo.control.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LoginThrottle {
    private static final int MAX_FAILURES = 5;
    private static final long LOCK_DURATION_MILLIS = 60_000;
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean locked(String address, long now) {
        Attempt attempt = attempts.get(address);
        return attempt != null && attempt.lockedUntil() > now;
    }

    public void succeeded(String address) {
        attempts.remove(address);
    }

    public void failed(String address, long now) {
        if (attempts.size() > 2048) attempts.clear(); // Límite de memoria frente a IPs arbitrarias.
        attempts.compute(
                address,
                (key, previous) -> {
                    Attempt current = previous == null ? new Attempt(0, 0) : previous;
                    int failures = current.failures() + 1;
                    return failures >= MAX_FAILURES
                            ? new Attempt(0, now + LOCK_DURATION_MILLIS)
                            : new Attempt(failures, current.lockedUntil());
                });
    }

    private record Attempt(int failures, long lockedUntil) {}
}
