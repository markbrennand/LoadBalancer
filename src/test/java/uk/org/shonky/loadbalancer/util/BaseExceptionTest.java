package uk.org.shonky.loadbalancer.util;

import java.util.ListResourceBundle;

import org.junit.*;

import static org.junit.Assert.*;

public class BaseExceptionTest {

    @Test
    public void testFormattedMessage() {
        Internationalisation.setBundle(new TestResources());
        BaseException be = new BaseException("TestKey", "ABC", 15);
        assertEquals("TEST 'ABC' HAS 15 ERRORS", be.getMessage());
    }

    private static class TestResources extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                    {"TestKey", "TEST ''{0}'' HAS {1} ERRORS"}
            };
        }
    }
}
