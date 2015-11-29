package uk.org.shonky.loadbalancer.engine.config;

import uk.org.shonky.loadbalancer.util.BaseException;

public class ConfigurationException extends BaseException {

    public ConfigurationException(String key) {
        super(key);
    }

    public ConfigurationException(String key, Object... args) {
        super(key, args);
    }
}
