package uk.org.shonky.loadbalancer.engine.config;

import java.util.Map;

import uk.org.shonky.loadbalancer.engine.policy.Connector;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Preconditions.checkNotNull;

public class Service {
    private String name;
    private ConnectorPolicy policy;
    private Map<String, String> config;
    private Endpoint endpoint;
    private Connector connector;

    public Service(String name, Configuration config, ConnectorPolicy policy) {
        this.name = checkNotNull(name);
        this.config = checkNotNull(config).getConfiguration(name+ ".service.");
        this.policy = checkNotNull(policy);

        String address = this.config.get("listen.address");
        if (isNullOrEmpty(address)) {
            throw new ConfigurationException("Listening address not set for service {0}", name);
        }
        endpoint =  Endpoint.parse(address, true);

        connector = policy.newConnector(this);
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getConfiguration() {
        return config;
    }

    public Endpoint getListeningEndpoint() {
        return endpoint;
    }

    public Connector getConnector() {
        return connector;
    }
}