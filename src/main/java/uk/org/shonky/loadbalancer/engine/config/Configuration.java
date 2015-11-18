package uk.org.shonky.loadbalancer.engine.config;

import java.util.Map;

public interface Configuration {
    public Map<String, String> getConfiguration(String prefix);
}
