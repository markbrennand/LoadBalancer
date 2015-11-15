package uk.org.shonky.loadbalancer.engine.config;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class PropertiesConfiguration implements Configuration {
    private Properties properties;

    public PropertiesConfiguration(Properties properties) {
        this.properties = checkNotNull(properties);
    }

    @Override
    public Properties getPropertiesWithPrefix(String prefix) {
        Properties retProperties = new Properties();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                retProperties.put(key.replace(prefix, ""), properties.get(key));
            }
        }
        return retProperties;
    }
}
