package uk.org.shonky.loadbalancer.webservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.org.shonky.loadbalancer.engine.ProcessorState;
import uk.org.shonky.loadbalancer.services.MonitoringService;

import java.util.List;

@RestController
public class MonitoringController {
    @Autowired
    private MonitoringService monitoringService;

    @RequestMapping("/monitoring/states")
    public List<ProcessorState> states() {
        return monitoringService.getStates();
    }
}
