package uy.edu.isbo.control.web.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FormFieldsTest {
    @Test
    void trimsAndChecksRequiredFields() {
        assertEquals("Laboratorio 1", FormFields.required(" Laboratorio 1 ", "name"));
        assertThrows(IllegalArgumentException.class, () -> FormFields.required("   ", "name"));
        assertThrows(IllegalArgumentException.class, () -> FormFields.required("x".repeat(101), "name"));
        assertThrows(IllegalArgumentException.class, () -> FormFields.required("a\nInjected", "name"));
        assertThrows(IllegalArgumentException.class, () -> FormFields.required("x".repeat(81), "room", 80));
    }

    @Test
    void acceptsOnlyPositiveIds() {
        assertEquals(0, FormFields.optionalId(null));
        assertEquals(0, FormFields.optionalId("0"));
        assertEquals(0, FormFields.optionalId(""));
        assertEquals(7, FormFields.positiveId("7"));
        for (String value : new String[] {"0", "-1", "1.2", "bad", "9223372036854775808"}) {
            assertThrows(IllegalArgumentException.class, () -> FormFields.positiveId(value));
        }
    }
}
