package uk.org.shonky.loadbalancer.dao;

import java.util.List;

import uk.org.shonky.loadbalancer.engine.ProcessorState;
import uk.org.shonky.loadbalancer.engine.ProcessorThread;

public interface MonitoringDAO {
    public void addProcessorThread(ProcessorThread thread);
    public void removeProcessorThread(ProcessorThread thread);
    public List<ProcessorState> getStates();
}