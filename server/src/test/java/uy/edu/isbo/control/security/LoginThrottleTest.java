package uy.edu.isbo.control.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoginThrottleTest {
    private final LoginThrottle throttle = new LoginThrottle();
    private static final String ADDRESS = "192.168.1.10";

    @Test
    void locksAfterFiveFailuresAndAllowsRetryAfterOneMinute() {
        for (int attempt = 0; attempt < 4; attempt++) {
            throttle.failed(ADDRESS, 1_000);
            assertFalse(throttle.locked(ADDRESS, 1_000));
        }
        throttle.failed(ADDRESS, 1_000);
        assertTrue(throttle.locked(ADDRESS, 60_999));
        assertFalse(throttle.locked(ADDRESS, 61_000));
    }

    @Test
    void successfulLoginResetsPreviousFailures() {
        for (int attempt = 0; attempt < 4; attempt++) {
            throttle.failed(ADDRESS, 1_000);
        }
        throttle.succeeded(ADDRESS);
        throttle.failed(ADDRESS, 1_000);
        assertFalse(throttle.locked(ADDRESS, 1_000));
    }

    @Test
    void lockDoesNotAffectOtherAddresses() {
        for (int attempt = 0; attempt < 5; attempt++) {
            throttle.failed(ADDRESS, 1_000);
        }
        assertFalse(throttle.locked("192.168.1.11", 1_000));
    }
}
