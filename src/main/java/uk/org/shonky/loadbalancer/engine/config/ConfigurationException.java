package uk.org.shonky.loadbalancer.engine.config;

import uk.org.shonky.loadbalancer.util.BaseException;

public class ConfigurationException extends BaseException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Object... args) {
        super(message, args);
    }
}
