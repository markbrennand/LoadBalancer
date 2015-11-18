package uk.org.shonky.loadbalancer.engine;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.Set;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.shonky.loadbalancer.engine.net.Listener;
import uk.org.shonky.loadbalancer.engine.net.Processor;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;



import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.ImmutableSet.copyOf;

public class ProcessorThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ProcessorThread.class);

    private Selector selector;

    public ProcessorThread() throws IOException {
        this.selector = Selector.open();
    }

    public void addListener(Listener listener) throws IOException {
        listener.register(selector);
    }

    public void run() {
        for (;;) {
            Processor processor = null;
            try {
                selector.select(purgeExpired());
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                while (iter.hasNext()) {
                    processor = (Processor) iter.next().attachment();
                    processor.process(selector);
                    processor = null;
                    iter.remove();
                }

            } catch (Exception e) {
                logger.error("Processing failure", e);
                if (processor != null) {
                    processor.terminate();
                }
            }
        }
    }

    public Set<ProcessorState> getStates() {
        Set<SelectionKey> keys = selector.keys();
        Set<ProcessorState> retSet = newHashSet();
        long now = System.currentTimeMillis();
        for (SelectionKey key : keys) {
            Processor processor = (Processor) key.attachment();
            retSet.add(new ProcessorState(processor.getId(), toOpsString(key.interestOps()), processor.getExpiry() - now));
        }
        return copyOf(retSet);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessorThread) {
            return ((ProcessorThread) obj).selector.equals(selector);
        } else {
            return false;
        }
    }

    private long purgeExpired() {
        long now = System.currentTimeMillis();
        long minWaitTime = Long.MAX_VALUE - now;
        for (SelectionKey key : selector.keys()) {
            Processor processor = (Processor) key.attachment();
            long waitTime = processor.getExpiry() - now;
            if (waitTime <= 0) {
                processor.terminate();
            } else if (waitTime < minWaitTime) {
                minWaitTime = waitTime;
            }
        }
        return minWaitTime;
    }

    private String toOpsString(int ops) {
        switch(ops & (OP_CONNECT | OP_ACCEPT | OP_READ | OP_WRITE)) {
            case OP_CONNECT:
                return "CONNECT";

            case OP_ACCEPT:
                return "ACCEPT";

            case OP_READ | OP_WRITE:
                return "READ+WRITE";

            case OP_READ:
                return "READ";

            case OP_WRITE:
                return "WRITE";

            default:
                return "NONE";
        }
    }
}