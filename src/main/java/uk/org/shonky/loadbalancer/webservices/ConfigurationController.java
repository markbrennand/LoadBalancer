package uk.org.shonky.loadbalancer.webservices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.services.ConfigurationService;


@RestController
public class ConfigurationController {
    @Autowired
    private ConfigurationService configService;

    @RequestMapping("/configuration/forwarders")
    public Response<List<Forwarder>> forwarders() {
        return new Response<List<Forwarder>>() {
            @Override
            public List<Forwarder> run() {
                return configService.getForwarders();
            }
        }.invoke();
    }

    @RequestMapping("/configuration/forwarder")
    public Response<Forwarder> forwarder(@RequestParam(value="name") final String name) {
        return new Response<Forwarder>() {
            @Override
            public Forwarder run() {
                return configService.getForwarder(name);
            }
        }.invoke();
    }

    @RequestMapping("/configuration/policies")
    public Response<List<ConnectorPolicy>> policies() {
        return new Response<List<ConnectorPolicy>>() {
            @Override
            public List<ConnectorPolicy> run() {
                return configService.getConnectorPolicies();
            }
        }.invoke();
    }
}
