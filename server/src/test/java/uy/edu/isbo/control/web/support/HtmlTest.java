package uy.edu.isbo.control.web.support;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class HtmlTest {
    @Test void escapesUntrustedNamesAndAttributes() {
        assertEquals("&lt;script&gt;&amp;&quot;&#39;", Html.escape("<script>&\"'"));
        assertEquals("", Html.escape(null));
    }
}
