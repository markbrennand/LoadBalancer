package uk.org.shonky.loadbalancer.engine.config;

import java.util.Properties;

import org.junit.*;

import static org.junit.Assert.*;

public class PropertiesConfiguratorTest {

    @Test
    public void testAll() {
        Properties props = new Properties();
        props.put("TEST.a", "one");
        props.put("TEST.b.c", "two");
        props.put("TEST2.a", "three");
        props.put("NOTTEST2.b.c", "four");

        PropertiesConfiguration config = new PropertiesConfiguration(props);

        assertEquals(2, config.getPropertiesWithPrefix("TEST.").size());
        assertEquals("one", config.getPropertiesWithPrefix("TEST.").get("a"));
        assertEquals("two", config.getPropertiesWithPrefix("TEST.").get("b.c"));

        assertEquals(3, config.getPropertiesWithPrefix("TEST").size());
        assertEquals("one", config.getPropertiesWithPrefix("TEST").get(".a"));
        assertEquals("two", config.getPropertiesWithPrefix("TEST").get(".b.c"));
        assertEquals("three", config.getPropertiesWithPrefix("TEST").get("2.a"));
    }
}
