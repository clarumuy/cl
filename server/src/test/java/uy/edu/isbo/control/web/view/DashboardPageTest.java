package uy.edu.isbo.control.web.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DashboardPageTest {
    @ParameterizedTest
    @ValueSource(strings = {"overview", "labs", "pcs", "pending", "users"})
    void acceptsExistingNavigationRoutes(String parameter) {
        assertEquals(parameter, DashboardPage.from(parameter).parameter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "../login", "unknown", "USERS"})
    void unexpectedRoutesUseTheOverview(String parameter) {
        assertSame(DashboardPage.OVERVIEW, DashboardPage.from(parameter));
    }

    @Test
    void missingRouteUsesTheOverview() {
        assertSame(DashboardPage.OVERVIEW, DashboardPage.from(null));
    }
}
