package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import uk.org.shonky.loadbalancer.engine.config.Connector;
import uk.org.shonky.loadbalancer.util.Allocator;
import uk.org.shonky.loadbalancer.engine.config.Endpoint;

import static java.nio.channels.SelectionKey.OP_READ;

import static com.google.common.base.Preconditions.checkNotNull;

public class Session {
    private Connector connector;
    private Endpoint endpoint;
    private Connection sourceConnection;
    private Connection destinationConnection;

    public Session(SocketChannel source, Connector connector, Selector selector,int maxQueueSize,
                   Allocator<ByteBuffer> allocator)
            throws IOException
    {
        this.connector = checkNotNull(connector);
        this.endpoint = connector.nextEndpoint();

        Socket sourceSocket = source.socket();
        SocketChannel destinationChannel = endpoint.connect();
        Socket destinationSocket = destinationChannel.socket();

        String from = new StringBuffer(sourceSocket.getInetAddress().getHostAddress()).
                append("(").
                append(sourceSocket.getLocalPort()).
                append(")").
                toString();

        String to = new StringBuffer(destinationSocket.getInetAddress().getHostAddress()).
                append("(").
                append(destinationSocket.getPort()).
                append(")").
                toString();

        sourceConnection = new Connection(
                new StringBuffer(from).append(" -> ").append(to).toString(),
                this,
                true,
                destinationChannel,
                maxQueueSize,
                allocator);

        destinationConnection = new Connection(
                new StringBuffer(from).append(" <- ").append(to).toString(),
                this,
                false,
                source,
                maxQueueSize,
                allocator);

        source.register(selector, OP_READ, destinationConnection);
        destinationChannel.register(selector, OP_READ, sourceConnection);
        sourceConnection.register(selector);
        destinationConnection.register(selector);
    }

    public void append(boolean source, ByteBuffer buffer) {
        if (source) {
            sourceConnection.append(buffer);
        } else {
            destinationConnection.append(buffer);
        }
    }

    public void enableRead(boolean source, boolean enabled) {
        if (source) {
            sourceConnection.enableRead(enabled);
        } else {
            destinationConnection.enableRead(enabled);
        }
    }

    public void terminate() {
        sourceConnection.terminate();
        destinationConnection.terminate();
        connector.endpointClosed(endpoint);
    }
}
