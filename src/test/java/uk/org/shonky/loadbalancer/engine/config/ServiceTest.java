package uk.org.shonky.loadbalancer.engine.config;

import java.util.Properties;
import java.net.InetSocketAddress;

import org.junit.*;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.engine.policy.impl.RoundRobinPolicy;

import static org.junit.Assert.*;

public class ServiceTest {

    @Test
    public void testAllInterfaces() {
        ConnectorPolicy policy = new RoundRobinPolicy();
        Properties props = new Properties();
        props.put("TEST.service.listen.address", "7001");
        props.put("TEST.service.forward.addresses", "localhost:1, localhost:2, localhost:3");
        props.put("TEST2.service.listen.address", "7002");
        props.put("TEST2.service.forward.addresses", "localhost:4, localhost:5, localhost:6");

        Service service = new Service("TEST", new PropertiesConfiguration(props), policy);
        Endpoint endpoint = service.getListeningEndpoint();

        assertEquals("TEST", service.getName());
        assertEquals("0.0.0.0", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(7001, ((InetSocketAddress) endpoint.getAddress()).getPort());

        endpoint = service.getConnector().nextEndpoint();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());

        endpoint = service.getConnector().nextEndpoint();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(2, ((InetSocketAddress) endpoint.getAddress()).getPort());

        endpoint = service.getConnector().nextEndpoint();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(3, ((InetSocketAddress) endpoint.getAddress()).getPort());

        endpoint = service.getConnector().nextEndpoint();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());
    }

    @Test
    public void testSpecificInterface() {
        ConnectorPolicy policy = new RoundRobinPolicy();
        Properties props = new Properties();
        props.put("TEST.service.listen.address", "localhost:7001");
        props.put("TEST.service.forward.addresses", "localhost:1, localhost:2, localhost:3");
        props.put("TEST2.service.listen.address", "localhost:7002");
        props.put("TEST2.service.forward.addresses", "localhost:4, localhost:5, localhost:6");

        Service service = new Service("TEST", new PropertiesConfiguration(props), policy);
        Endpoint endpoint = service.getListeningEndpoint();

        assertEquals("TEST", service.getName());
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(7001, ((InetSocketAddress) endpoint.getAddress()).getPort());

        endpoint = service.getConnector().nextEndpoint();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());

        endpoint = service.getConnector().nextEndpoint();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(2, ((InetSocketAddress) endpoint.getAddress()).getPort());

        endpoint = service.getConnector().nextEndpoint();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(3, ((InetSocketAddress) endpoint.getAddress()).getPort());

        endpoint = service.getConnector().nextEndpoint();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());
    }

    @Test
    public void testMissingListeningAddress() {
        try {
            ConnectorPolicy policy = new RoundRobinPolicy();
            Properties props = new Properties();
            props.put("TEST.service.not.listen.address", "localhost:7001");
            props.put("TEST.service.forward.addresses", "localhost:1, localhost:2, localhost:3");
            props.put("TEST2.service.listen.address", "localhost:7002");
            props.put("TEST2.service.forward.addresses", "localhost:4, localhost:5, localhost:6");

            Service service = new Service("TEST", new PropertiesConfiguration(props), policy);
            fail();
        } catch(ConfigurationException ce) {
        }
    }
}
