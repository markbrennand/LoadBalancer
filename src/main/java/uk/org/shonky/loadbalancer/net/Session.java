package uk.org.shonky.loadbalancer.net;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.OP_READ;

public class Session {
    private Connection sourceConnection;
    private Connection destinationConnection;

    public Session(SocketChannel source, SocketChannel destination, Selector selector,int maxQueueSize)
            throws IOException
    {
        source.configureBlocking(false);
        destination.configureBlocking(false);
        this.sourceConnection = new Connection(destination, maxQueueSize);
        this.destinationConnection = new Connection(source, maxQueueSize);
        source.register(selector, OP_READ, this.destinationConnection);
        destination.register(selector, OP_READ, this.sourceConnection);
    }
}
