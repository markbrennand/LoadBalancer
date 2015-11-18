package uk.org.shonky.loadbalancer.engine.config;

import java.util.Map;
import java.util.Properties;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.base.Preconditions.checkNotNull;

public class PropertiesConfiguration implements Configuration {
    private Properties properties;

    public PropertiesConfiguration(Properties properties) {
        this.properties = checkNotNull(properties);
    }

    @Override
    public Map<String, String> getConfiguration(String prefix) {
        Map<String, String> retMap = newHashMap();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                retMap.put(key.replace(prefix, ""), properties.getProperty(key));
            }
        }
        return copyOf(retMap);
    }
}
