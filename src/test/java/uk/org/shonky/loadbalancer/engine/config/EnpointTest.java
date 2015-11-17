package uk.org.shonky.loadbalancer.engine.config;

import org.junit.*;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;

public class EnpointTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testServerEndpoint() {
        Endpoint endpoint = Endpoint.parse("1024", true);
        assertEquals(1024, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertEquals("0.0.0.0", ((InetSocketAddress) endpoint.getAddress()).getAddress().getHostAddress());

        endpoint = Endpoint.parse("127.0.0.1:1025", true);
        assertEquals(1025, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertEquals("127.0.0.1", ((InetSocketAddress) endpoint.getAddress()).getAddress().getHostAddress());

        endpoint = Endpoint.parse("localhost:1026", true);
        assertEquals(1026, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertEquals("127.0.0.1", ((InetSocketAddress) endpoint.getAddress()).getAddress().getHostAddress());

        try {
            endpoint = Endpoint.parse("", true);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("1a", true);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("127.0.0.256:1027", true);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("nosuchhost:1028", true);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("0", true);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("65536", true);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("localhost:0", true);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("localhost:65536", true);
            fail();
        } catch(ConfigurationException ce) {
        }
    }

    @Test
    public void testForwardingEndpoint() {
        Endpoint endpoint = Endpoint.parse("127.0.0.1:1024", false);
        assertEquals(1024, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertEquals("127.0.0.1", ((InetSocketAddress) endpoint.getAddress()).getAddress().getHostAddress());

        endpoint = Endpoint.parse("localhost:1025", false);
        assertEquals(1025, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertEquals("127.0.0.1", ((InetSocketAddress) endpoint.getAddress()).getAddress().getHostAddress());

        try {
            endpoint = Endpoint.parse("", false);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("1026", false);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("localhost:1a", false);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("127.0.0.256:1027", false);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("nosuchhost:1028", false);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("localhost:0", false);
            fail();
        } catch(ConfigurationException ce) {
        }

        try {
            endpoint = Endpoint.parse("localhost:65536", false);
            fail();
        } catch(ConfigurationException ce) {
        }
    }
}