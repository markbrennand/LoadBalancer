package uk.org.shonky.loadbalancer.engine.config;

import java.util.Map;

import uk.org.shonky.loadbalancer.engine.policy.Connector;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Preconditions.checkNotNull;

public class Forwarder {
    private String name;
    private String policyName;
    private Map<String, String> config;
    private Endpoint endpoint;
    private Connector connector;

    public Forwarder(String name, Configuration config) {
        this.name = checkNotNull(name);
        this.config = checkNotNull(config).getConfiguration(name + ".forwarder.");

        String address = this.config.get("listen.address");
        if (isNullOrEmpty(address)) {
            throw new ConfigurationException("ForwarderMissingListeningAddress", name);
        }
        this.endpoint = Endpoint.parse(address, true);

        policyName = this.config.get("connector.policy");
        if (isNullOrEmpty(policyName)) {
            throw new ConfigurationException("ForwarderMissingConnectorPolicy", name);
        }
    }

    public String getName() {
        return name;
    }

    public void initialiseConnector(ConnectorPolicy policy) {
        connector = policy.newConnector(this);
    }

    public Map<String, String> getConfiguration() {
        return config;
    }

    public Endpoint getListeningEndpoint() {
        return endpoint;
    }

    public String getConnectorPolicyName() {
        return policyName;
    }

    public Connector getConnector() {
        return connector;
    }
}