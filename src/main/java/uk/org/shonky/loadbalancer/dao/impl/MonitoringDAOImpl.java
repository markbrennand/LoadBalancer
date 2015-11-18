package uk.org.shonky.loadbalancer.dao.impl;

import java.util.List;
import java.nio.channels.Selector;

import org.springframework.stereotype.Repository;

import uk.org.shonky.loadbalancer.dao.MonitoringDAO;
import uk.org.shonky.loadbalancer.engine.ProcessorState;
import uk.org.shonky.loadbalancer.engine.ProcessorThread;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.ImmutableList.copyOf;

@Repository("MonitoringDAO")
public class MonitoringDAOImpl implements MonitoringDAO {
    private List<ProcessorThread> threads = newArrayList();

    @Override
    public void addProcessorThread(ProcessorThread thread) {
        if (!threads.contains(thread)) {
            threads.add(thread);
        }
    }

    @Override
    public void removeProcessorThread(ProcessorThread thread) {
        threads.remove(thread);
    }

    @Override
    public synchronized List<ProcessorState> getStates() {
        List<ProcessorState> states = newArrayList();
        for (ProcessorThread thread : threads) {
            states.addAll(thread.getStates());
        }

        return copyOf(states);
    }
}
