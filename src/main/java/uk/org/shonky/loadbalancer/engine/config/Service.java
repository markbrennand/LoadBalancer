package uk.org.shonky.loadbalancer.engine.config;

import java.net.InetAddress;
import java.util.Properties;

import org.apache.log4j.Logger;
import uk.org.shonky.loadbalancer.engine.policy.Connector;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Preconditions.checkNotNull;

public class Service {
    private final static Logger logger = Logger.getLogger(Service.class);

    private String name;
    private ConnectorPolicy policy;
    private Properties config;
    private Endpoint endpoint;
    private Connector connector;

    public Service(String name, Configuration config, ConnectorPolicy policy) {
        this.name = checkNotNull(name);
        this.config = checkNotNull(config).getPropertiesWithPrefix(name+ ".service.");
        this.policy = checkNotNull(policy);

        String address = this.config.getProperty("listen.address");
        if (isNullOrEmpty(address)) {
            throw new ConfigurationException("Listening address not set for service '" + name + "'");
        }
        endpoint =  Endpoint.parse(address, true);

        connector = policy.newConnector(this.config);
    }

    public String getName() {
        return name;
    }

    public Endpoint getListeningEndpoint() {
        return endpoint;
    }

    public Connector getConnector() {
        return connector;
    }
}