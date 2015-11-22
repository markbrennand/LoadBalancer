package uk.org.shonky.loadbalancer.webservices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.services.ConfigurationService;


@RestController
public class ConfigurationController {
    @Autowired
    private ConfigurationService configService;

    @RequestMapping("/configuration/forwarders")
    public List<Forwarder> services() {
        return configService.getForwarders();
    }

    @RequestMapping("/configuration/policies")
    public List<ConnectorPolicy> policies() {
        return configService.getConnectorPolicies();
    }
}
