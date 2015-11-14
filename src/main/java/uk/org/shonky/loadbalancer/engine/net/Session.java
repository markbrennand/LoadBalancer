package uk.org.shonky.loadbalancer.engine.net;

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
        sourceConnection = new Connection(destination, maxQueueSize);
        destinationConnection = new Connection(source, maxQueueSize);
        source.register(selector, OP_READ, destinationConnection);
        destination.register(selector, OP_READ, sourceConnection);
        sourceConnection.register(selector);
        destinationConnection.register(selector);
    }
}
