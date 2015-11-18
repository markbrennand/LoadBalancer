package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.nio.channels.Selector;

public interface Processor {
    public String getId();
    public long getExpiry();
    public void process(Selector selector) throws IOException;
    public void terminate();
}
