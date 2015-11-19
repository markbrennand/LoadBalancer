package uk.org.shonky.loadbalancer.engine.config;

import java.util.Properties;
import java.net.InetSocketAddress;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.engine.policy.impl.RoundRobinPolicy;
import uk.org.shonky.loadbalancer.services.ConfigurationService;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext.xml" })
public class ServiceTest {
    @Autowired
    private ConfigurationService configurationService;

    @Test
    public void testAllInterfaces() {
        Properties props = new Properties();
        props.put("TEST.service.listen.address", "7001");
        props.put("TEST.service.forward.addresses", "localhost:1, localhost:2, localhost:3");
        props.put("TEST.service.expiry", "1000");
        props.put("TEST.service.connector.policy", "Round Robin Policy");
        props.put("TEST2.service.listen.address", "7002");
        props.put("TEST2.service.forward.addresses", "localhost:4, localhost:5, localhost:6");
        props.put("TEST2.service.connector.policy", "Round Robin Policy");

        Service service = new Service("TEST", new PropertiesConfiguration(props));
        service.initialiseConnector(configurationService.getConnectorPolicy(service.getConnectorPolicyName()));
        Endpoint endpoint = service.getListeningEndpoint();

        assertEquals("TEST", service.getName());
        assertEquals("0.0.0.0", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(7001, ((InetSocketAddress) endpoint.getAddress()).getPort());

        Endpoints endpoints = service.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = service.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(2, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = service.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(3, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = service.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());
    }

    @Test
    public void testSpecificInterface() {
        Properties props = new Properties();
        props.put("TEST.service.listen.address", "localhost:7001");
        props.put("TEST.service.forward.addresses", "localhost:1, localhost:2, localhost:3");
        props.put("TEST.service.expiry", "1000");
        props.put("TEST.service.connector.policy", "Round Robin Policy");
        props.put("TEST2.service.listen.address", "localhost:7002");
        props.put("TEST2.service.forward.addresses", "localhost:4, localhost:5, localhost:6");
        props.put("TEST2.service.connector.policy", "Round Robin Policy");

        Service service = new Service("TEST", new PropertiesConfiguration(props));
        service.initialiseConnector(configurationService.getConnectorPolicy(service.getConnectorPolicyName()));
        Endpoint endpoint = service.getListeningEndpoint();

        assertEquals("TEST", service.getName());
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(7001, ((InetSocketAddress) endpoint.getAddress()).getPort());

        Endpoints endpoints = service.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(1, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = service.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(2, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = service.getConnector().nextConnectionEndpoints();
        endpoint = endpoints.next();
        assertEquals("localhost", ((InetSocketAddress) endpoint.getAddress()).getHostName());
        assertEquals(3, ((InetSocketAddress) endpoint.getAddress()).getPort());
        assertNull(endpoints.next());

        endpoints = service.getConnector().nextConnectionEndpoints();
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
            props.put("TEST.service.not.listen.address", "localhost:7001");
            props.put("TEST.service.forward.addresses", "localhost:1, localhost:2, localhost:3");
            props.put("TEST2.service.listen.address", "localhost:7002");
            props.put("TEST2.service.forward.addresses", "localhost:4, localhost:5, localhost:6");

            Service service = new Service("TEST", new PropertiesConfiguration(props));
            fail();
        } catch(ConfigurationException ce) {
        }
    }
}