package uk.org.shonky.loadbalancer.engine.config;

import java.util.Properties;

public interface Configuration {
    public Properties getPropertiesWithPrefix(String prefix);
}
