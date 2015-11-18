package uk.org.shonky.loadbalancer.services;

import java.util.List;

import uk.org.shonky.loadbalancer.engine.ProcessorState;

public interface MonitoringService {
    public List<ProcessorState> getStates();
}