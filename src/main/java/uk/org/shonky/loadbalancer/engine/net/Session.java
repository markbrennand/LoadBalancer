package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import uk.org.shonky.loadbalancer.util.Allocator;
import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.policy.Connector;

import static com.google.common.base.Preconditions.checkNotNull;

public class Session {
    private Connector connector;
    private Endpoint endpoint;
    private Connection sourceConnection;
    private Connection destinationConnection;
    private boolean endpointReleased;
    private long expiry;
    private long lastActive;

    public Session(SocketChannel source, Connector connector, Selector selector, int maxQueueSize,
                   Allocator<ByteBuffer> allocator)
            throws IOException
    {
        this.connector = checkNotNull(connector);
        this.endpoint = connector.nextEndpoint();

        Socket sourceSocket = source.socket();
        SocketChannel destination = endpoint.connect();
        Socket destinationSocket = destination.socket();

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
                selector,
                destination,
                maxQueueSize,
                allocator);

        destinationConnection = new Connection(
                new StringBuffer(from).append(" <- ").append(to).toString(),
                this,
                false,
                selector,
                source,
                maxQueueSize,
                allocator);

        active();
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
            sourceConnection.enableReceive(enabled);
        } else {
            destinationConnection.enableReceive(enabled);
        }
    }

    public synchronized void closing(boolean source) {
        if (source) {
            sourceConnection.close();
        } else {
            destinationConnection.close();
        }
        if (!endpointReleased) {
            connector.endpointClosed(endpoint);
            endpointReleased = true;
        }
    }

    public void terminate() {
        sourceConnection.kill();
        destinationConnection.kill();
    }

    public void active() {
        this.lastActive = System.currentTimeMillis();
    }

    public long expiry() {
        return lastActive + connector.getExpiry();
    }
}