package uk.org.shonky.loadbalancer.engine.config;

import java.net.InetAddress;
import java.util.Properties;

import org.apache.log4j.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;

public abstract class AbstractConnector implements Connector {
   private final static Logger logger = Logger.getLogger(AbstractConnector.class);

    private InetAddress listeningAddress;
    private int listeningPort;

    @Override
    public InetAddress getListeningAddress() {
        return listeningAddress;
    }

    @Override
    public int getListeningPort() {
        return listeningPort;
    }

    protected void configure(String id, Properties config) {
        String address = config.getProperty(id + ".listen.address");
        if (isNullOrEmpty(address)) {
            throw new ConfigurationException("Listening address not set for service '" + id + "'");
        }

        String port = config.getProperty(id + ".listen.port");
        if (isNullOrEmpty(port)) {
            throw new ConfigurationException("Listening port not set for service '" + id + "'");
        }

        try {
            listeningAddress = InetAddress.getByName(address);
        } catch(Exception e) {
            logger.error("Failed to map address '" + address +"'", e);
            throw new ConfigurationException("Address '" + address + "' for service '" + id +
                    "' could not be resolved");
        }

        try {
            listeningPort = Integer.parseInt(port);
        } catch(NumberFormatException nfe) {
            throw new ConfigurationException("Port '" + port + "' for service '" + id +
                    "' is not numeric");
        }

        if (listeningPort < 1 || listeningPort > 65535) {
            throw new ConfigurationException("Port '" + port + "' for service '" + id +
                    "' invalid, must be in range 1 .. 65535");
        }
    }
}
