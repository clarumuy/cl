package uy.edu.isbo.control.agent;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AgentClientTest {
    @Test void parsesOnlyKnownCommands() {
        assertNull(AgentClient.parse("none"));
        assertEquals(new AgentClient.Offer(19,15000),AgentClient.parse("shutdown|19|15000"));
        for(String value:new String[]{"shutdown","reboot|1|100","shutdown|0|100","shutdown|2|-1","shutdown|2|61000"})
            assertThrows(RuntimeException.class,()->AgentClient.parse(value));
    }
}
