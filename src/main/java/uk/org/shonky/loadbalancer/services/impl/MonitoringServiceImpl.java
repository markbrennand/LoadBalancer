package uk.org.shonky.loadbalancer.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.shonky.loadbalancer.dao.MonitoringDAO;
import uk.org.shonky.loadbalancer.engine.ProcessorState;
import uk.org.shonky.loadbalancer.services.MonitoringService;

import java.util.List;

@Service("MonitoringService")
public class MonitoringServiceImpl implements MonitoringService {
    @Autowired
    private MonitoringDAO monitoringDAO;

    @Override
    public List<ProcessorState> getStates() {
        return monitoringDAO.getStates();
    }
}
