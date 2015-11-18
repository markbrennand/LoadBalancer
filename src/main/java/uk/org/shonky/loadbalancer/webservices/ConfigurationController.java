package uk.org.shonky.loadbalancer.webservices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.services.ConfigurationService;


@RestController
public class ConfigurationController {
    @Autowired
    private ConfigurationService configService;

    @RequestMapping("/configuration/services")
    public List<Service> services() {
        return configService.getServices();
    }

}
