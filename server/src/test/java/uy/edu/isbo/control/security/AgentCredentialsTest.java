package uy.edu.isbo.control.security;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AgentCredentialsTest {
    @Test void neverAcceptsOtherTokensOrNullHash() {
        String secret = "a".repeat(64);
        String hash = AgentCredentials.hash(secret);
        assertEquals(64, hash.length());
        assertTrue(AgentCredentials.matches(hash, secret));
        assertFalse(AgentCredentials.matches(hash, "b".repeat(64)));
        assertFalse(AgentCredentials.matches(null, secret));
    }
}
