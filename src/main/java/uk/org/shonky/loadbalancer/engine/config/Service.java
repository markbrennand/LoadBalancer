package uk.org.shonky.loadbalancer.engine.config;

import java.net.InetAddress;
import java.util.Properties;

import org.apache.log4j.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Preconditions.checkNotNull;

public class Service {
    private final static Logger logger = Logger.getLogger(Service.class);

    private String name;
    private ConnectorPolicy policy;
    private Properties config;

    public Service(String name, Configuration config, ConnectorPolicy policy) {
        this.name = checkNotNull(name);
        this.config = checkNotNull(config).getPropertiesWithPrefix(name+ ".service.");
        this.policy = checkNotNull(policy);
    }

    public String getName() {
        return name;
    }

    public InetAddress getListeningAddress() {
        String address = config.getProperty("listen.address");
        if (isNullOrEmpty(address)) {
            throw new ConfigurationException("Listening address not set for service '" + name + "'");
        }

        try {
            return InetAddress.getByName(address);
        } catch(Exception e) {
            logger.error("Failed to map address '" + address +"'", e);
            throw new ConfigurationException("Address '" + address + "' for service '" + name +
                    "' could not be resolved");
        }
    }

    public int getListeningPort() {
        String port = config.getProperty("listen.port");
        if (isNullOrEmpty(port)) {
            throw new ConfigurationException("Listening port not set for service '" + name + "'");
        }

        int listeningPort;
        try {
            listeningPort = Integer.parseInt(port);
        } catch(NumberFormatException nfe) {
            throw new ConfigurationException("Port '" + port + "' for service '" + name +
                    "' is not numeric");
        }

        if (listeningPort < 1 || listeningPort > 65535) {
            throw new ConfigurationException("Port '" + port + "' for service '" + name +
                    "' invalid, must be in range 1 .. 65535");
        }

        return listeningPort;
    }

    public Connector getConnector() {
        return policy.newConnector(config);
    }
}