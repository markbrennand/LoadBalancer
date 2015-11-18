package uk.org.shonky.loadbalancer.util;

import org.junit.*;

import static org.junit.Assert.*;

public class BaseExceptionTest {

    @Test
    public void testFormattedMessage() {
        BaseException be = new BaseException("TEST ''{0}'' HAS {1} ERRORS", "ABC", 15);
        assertEquals("TEST 'ABC' HAS 15 ERRORS", be.getMessage());
    }
}
