package uk.org.shonky.loadbalancer.engine.net;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public interface Processor {
    public Session process(Selector selector, SocketChannel channel) throws Exception;
}
