package uk.org.shonky.loadbalancer.engine.config;

import java.util.Properties;
import java.net.InetSocketAddress;

import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.shonky.loadbalancer.util.test.SpringTestBase;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.engine.policy.impl.RoundRobinPolicy;
import uk.org.shonky.loadbalancer.services.ConfigurationService;

import static org.junit.Assert.*;

public class ForwarderTest extends SpringTestBase {
    @Autowired
    private ConfigurationService configurationService;

    @Test
    public void testAllInterfaces() {
        Properties props = new Properties();
        props.put("TEST.forwarder.listen.address", "7001");
        props.put("TEST.forwarder.forward.addresses", "localhost:1, localhost:2, localhost:3");
        props.put("TEST.forwarder.expiry", "1000");
        props.put("TEST.forwarder.connector.policy", "Round Robin Policy");
        props.put("TEST2.forwarder.listen.address", "7002");
        props.put("TEST2.forwarder.forward.addresses", "localhost:4, localhost:5, localhost:6");
        props.put("TEST2.forwarder.connector.policy", "Round Robin Policy");

        Forwarder forwarder = new Forwarder("TEST", new PropertiesConfiguration(props));
        forwarder.initialiseConnector(configurationService.getConnectorPolicy(forwarder.getConnectorPolicyName()));
        Endpoint endpoint = forwarder.getListeningEndpoint();

        assertEquals("TEST", forwarder.getName());
        assertEquals("0.0.0.0", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(7001, ((InetSocketAddress) endpoint.getAddress()).getPort());

        Endpoints endpoints = forwarder.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = forwarder.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(2, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = forwarder.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(3, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = forwarder.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());
    }

    @Test
    public void testSpecificInterface() {
        Properties props = new Properties();
        props.put("TEST.forwarder.listen.address", "localhost:7001");
        props.put("TEST.forwarder.forward.addresses", "localhost:1, localhost:2, localhost:3");
        props.put("TEST.forwarder.expiry", "1000");
        props.put("TEST.forwarder.connector.policy", "Round Robin Policy");
        props.put("TEST2.forwarder.listen.address", "localhost:7002");
        props.put("TEST2.forwarder.forward.addresses", "localhost:4, localhost:5, localhost:6");
        props.put("TEST2.forwarder.connector.policy", "Round Robin Policy");

        Forwarder forwarder = new Forwarder("TEST", new PropertiesConfiguration(props));
        forwarder.initialiseConnector(configurationService.getConnectorPolicy(forwarder.getConnectorPolicyName()));
        Endpoint endpoint = forwarder.getListeningEndpoint();

        assertEquals("TEST", forwarder.getName());
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(7001, ((InetSocketAddress) endpoint.getAddress()).getPort());

        Endpoints endpoints = forwarder.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = forwarder.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(2, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = forwarder.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(3, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = forwarder.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());
    }

    @Test
    public void testMissingListeningAddress() {
        try {
            ConnectorPolicy policy = new RoundRobinPolicy();
            Properties props = new Properties();
            props.put("TEST.forwarder.not.listen.address", "localhost:7001");
            props.put("TEST.forwarder.forward.addresses", "localhost:1, localhost:2, localhost:3");
            props.put("TEST2.forwarder.listen.address", "localhost:7002");
            props.put("TEST2.forwarder.forward.addresses", "localhost:4, localhost:5, localhost:6");

            Forwarder forwarder = new Forwarder("TEST", new PropertiesConfiguration(props));
            fail();
        } catch(ConfigurationException ce) {
        }
    }
}