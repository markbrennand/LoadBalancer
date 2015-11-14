package uk.org.shonky.loadbalancer.util;

import org.junit.*;
import static org.junit.Assert.*;

public class DeliveryQueueTest {
    private final static Integer ONE = new Integer(1);
    private final static Integer TWO = new Integer(2);
    private final static Integer THREE = new Integer(3);
    private final static Integer FOUR = new Integer(4);
    private final static Integer FIVE = new Integer(5);
    private final static Integer SIX = new Integer(6);


    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    @Test
    public void testAppend() {
        DeliveryQueue<Integer> queue = new DeliveryQueue<Integer>(5);
        queue.append(ONE);
        queue.append(TWO);
        queue.append(THREE);
        queue.append(FOUR);
        queue.append(FIVE);

        assertEquals(ONE, queue.pop());
        assertEquals(TWO, queue.pop());
        assertEquals(THREE, queue.pop());
        assertEquals(FOUR, queue.pop());
        assertEquals(FIVE, queue.pop());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testHead() {
        DeliveryQueue<Integer> queue = new DeliveryQueue<Integer>(5);
        queue.append(ONE);
        queue.append(TWO);
        queue.append(THREE);
        queue.append(FOUR);
        queue.append(FIVE);
        queue.pop();
        queue.head(SIX);

        assertEquals(SIX, queue.pop());
        assertEquals(TWO, queue.pop());
        assertEquals(THREE, queue.pop());
        assertEquals(FOUR, queue.pop());
        assertEquals(FIVE, queue.pop());
        assertTrue(queue.isEmpty());

    }

    @Test
    public void testBounds() {
        DeliveryQueue<Integer> queue = new DeliveryQueue<Integer>(4);
        try {
            queue.pop();
            fail();
        } catch(DeliveryQueueEmptyException dqee) {
        }

        queue.append(ONE);
        queue.append(TWO);
        queue.append(THREE);
        queue.append(FOUR);

        try {
            queue.append(FIVE);
            fail();
        } catch(DeliveryQueueFullException dqfe) {
        }

        try {
            queue.head(FIVE);
        } catch(DeliveryQueueFullException dqfe) {
        }
    }
}