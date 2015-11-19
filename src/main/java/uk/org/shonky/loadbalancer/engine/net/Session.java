package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import uk.org.shonky.loadbalancer.util.Allocator;
import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Endpoints;
import uk.org.shonky.loadbalancer.engine.policy.Connector;

import static com.google.common.base.Preconditions.checkNotNull;

public class Session {
    private Connector connector;
    private Endpoint sourceEndpoint;
    private Endpoint destinationEndpoint;
    private Connection sourceConnection;
    private Connection destinationConnection;
    private boolean endpointReleased;
    private long lastActive;

    public Session(Service service, Endpoint sourceEndpoint, SocketChannel source, Selector selector, int maxQueueSize,
                   Allocator<ByteBuffer> allocator)
            throws IOException
    {
        this.connector = checkNotNull(service).getConnector();
        this.sourceEndpoint = checkNotNull(sourceEndpoint);

        sourceConnection = new Connection(
                service,
                this,
                selector,
                source,
                maxQueueSize,
                allocator);

        destinationConnection = new Connection(
                service,
                this,
                selector,
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
        if (!endpointReleased && destinationEndpoint != null) {
            connector.endpointDisconnected(destinationEndpoint);
            endpointReleased = true;
        }
    }

    public void terminate() {
        if (!endpointReleased && destinationEndpoint != null) {
            connector.endpointDisconnected(destinationEndpoint);
            endpointReleased = true;
        }
        sourceConnection.kill();
        destinationConnection.kill();
    }

    public void active() {
        this.lastActive = System.currentTimeMillis();
    }

    public long expiry() {
        return lastActive + connector.getExpiry();
    }

    public Endpoint getSourceEndpoint() {
        return sourceEndpoint;
    }

    public Endpoint getDestinationEndpoint() {
        return destinationEndpoint;
    }

    public void setDestinationEndpoint(Endpoint destinationEndpoint) {
        this.destinationEndpoint = checkNotNull(destinationEndpoint);
    }
}