package uk.org.shonky.loadbalancer.engine.config;

import org.junit.*;

import static org.junit.Assert.*;

public class EndpointsTest {

    @Test
    public void testAll() {
        Endpoint endpoint1 = Endpoint.parse("localhost:7001", false);
        Endpoint endpoint2 = Endpoint.parse("localhost:7002", false);
        Endpoint endpoint3 = Endpoint.parse("localhost:7003", false);

        Endpoints endpoints = new Endpoints(new Endpoint[] {endpoint1, endpoint2, endpoint3}, 0, 2);
        assertEquals(endpoint1, endpoints.next());
        assertEquals(endpoint2, endpoints.next());
        assertEquals(endpoint3, endpoints.next());
        assertNull(endpoints.next());

        endpoints = new Endpoints(new Endpoint[] {endpoint1, endpoint2, endpoint3}, 1, 0);
        assertEquals(endpoint2, endpoints.next());
        assertEquals(endpoint3, endpoints.next());
        assertEquals(endpoint1, endpoints.next());
        assertNull(endpoints.next());

        endpoints = new Endpoints(new Endpoint[] {endpoint1, endpoint2, endpoint3}, 2, 1);
        assertEquals(endpoint3, endpoints.next());
        assertEquals(endpoint1, endpoints.next());
        assertEquals(endpoint2, endpoints.next());
        assertNull(endpoints.next());

        endpoints = new Endpoints(new Endpoint[] {endpoint1, endpoint2, endpoint3}, 2, 0);
        assertEquals(endpoint3, endpoints.next());
        assertEquals(endpoint1, endpoints.next());
        assertNull(endpoints.next());
    }

    @Test
    public void testOutOfBounds() {
        Endpoint endpoint1 = Endpoint.parse("localhost:7001", false);
        Endpoint endpoint2 = Endpoint.parse("localhost:7002", false);
        Endpoint endpoint3 = Endpoint.parse("localhost:7003", false);

        try {
            new Endpoints(new Endpoint[] {endpoint1, endpoint2, endpoint3}, -1, 2);
            fail();
        } catch(IndexOutOfBoundsException ioobe) {
        }

        try {
            new Endpoints(new Endpoint[] { endpoint1, endpoint2, endpoint3 }, 0, 3);
            fail();
        } catch(IndexOutOfBoundsException ioobe) {
        }
    }
}
