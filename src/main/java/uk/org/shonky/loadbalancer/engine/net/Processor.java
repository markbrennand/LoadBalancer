package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.nio.channels.Selector;

public interface Processor {
    public void process(Selector selector) throws IOException;
    public long expiry();
    public void terminate();
}
