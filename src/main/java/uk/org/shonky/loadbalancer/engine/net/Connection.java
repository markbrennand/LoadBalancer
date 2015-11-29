/**
 * (c) Mark Brennand, 2015
 */
package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.shonky.loadbalancer.util.Allocator;
import uk.org.shonky.loadbalancer.util.DeliveryQueue;
import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Endpoints;
import uk.org.shonky.loadbalancer.engine.config.Forwarder;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static java.nio.channels.SelectionKey.OP_CONNECT;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides a connection which can be managed by NIO.
 *
 * The owning session manages two connections. The source connection is the socket accepted from the forwarder
 * listener. The destination connection is the forwarded socket.
 *
 * Data available on the connection is read and added to the write queue of the peer connection. If the session
 * determines that the peer's write queue is full, this connection will have the NIO read operation disabled until the
 * peer has flushed it write queue.
 *
 * If the connection has data available on its write queue, the owning session will enavble the NIO write operation
 * for this connection. Once the write queue is flushed, the owning session will disable the write operation.
 */
public class Connection implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    private Forwarder forwarder;
    private Selector selector;
    private Session session;
    private boolean source;
    private DeliveryQueue<ByteBuffer> queue;
    private Allocator<ByteBuffer> allocator;
    private SocketChannel channel;
    private SelectionKey key;
    private Endpoints endpoints;
    private Endpoint endpoint;
    private boolean connected;
    private boolean closing;
    private boolean closed;

    /**
     * Source connection constructor.
     *
     * @param forwarder forwarder associated with the connection.
     * @param session owning session for this connection.
     * @param selector NIO selector to be used to manage this connection.
     * @param channel socket channel accepted by the listener.
     * @param maxQueueSize maximum number of messages to be queued on the write queue.
     * @param allocator allocator to be used for NIO buffers.
     * @throws IOException an error occurred when configuring the sockat channel.
     */
    public Connection(Forwarder forwarder, Session session, Selector selector, SocketChannel channel, int maxQueueSize,
                      Allocator<ByteBuffer> allocator)
            throws IOException
    {
        this.forwarder = checkNotNull(forwarder);
        this.session = checkNotNull(session);
        this.selector = checkNotNull(selector);
        this.channel = checkNotNull(channel);
        this.allocator = checkNotNull(allocator);
        this.queue = new DeliveryQueue<ByteBuffer>(maxQueueSize);
        this.key = this.channel.register(selector, 0, this);    // Disabled till the forward connection is made.
        this.source =
        this.connected = true;
        session.active();

        logger.info("{} source connection registered with selector {}", getId(), selector);
    }

    /**
     * Destination connection constructor.
     *
     * @param forwarder forwarder associated with the connection.
     * @param session owning session for this connection.
     * @param selector NIO selector to be used to manage this connection.
     * @param maxQueueSize maximum number of messages to be queued on the write queue.
     * @param allocator allocator to be used for NIO buffers.
     * @throws IOException an error occurred when configuring the sockat channel.
     */
    public Connection(Forwarder forwarder, Session session, Selector selector, int maxQueueSize,
                      Allocator<ByteBuffer> allocator)
            throws IOException
    {
        this.forwarder = checkNotNull(forwarder);
        this.session = checkNotNull(session);
        this.selector = checkNotNull(selector);
        this.allocator = checkNotNull(allocator);
        this.endpoints = forwarder.getConnector().nextConnectionEndpoints();
        this.endpoint = endpoints.next();
        this.channel = this.endpoint.connect();
        this.queue = new DeliveryQueue<ByteBuffer>(maxQueueSize);
        this.key = this.channel.register(selector, OP_CONNECT, this);
        session.setDestinationEndpoint(endpoint);
        this.source =
        this.connected = false;

        logger.info("{} connection initiated, registered with selector {}", getId(), selector);
    }

    public void append(ByteBuffer buffer) {
        logger.debug("{} queueing buffer of {} bytes", getId(), buffer.remaining());
        queue.append(buffer);
        session.enableRead(!source, queue.hasCapacity());
        enableTransmit(true);
    }

    public void enableReceive(boolean enabled) {
        if (key == null) {
            return;
        }

        if (enabled) {
            key.interestOps((key.interestOps() | OP_READ) & (OP_READ | OP_WRITE));
        } else {
            key.interestOps(key.interestOps() & OP_WRITE);
        }
    }

    public void close() {
        logger.info("{} closing", getId());
        closing = true;
        enableTransmit(true);
    }

    @Override
    public long getExpiry() {
        return session.expiry();
    }

    @Override
    public String getId() {
        return new StringBuffer(forwarder.getName()).
                append("[").
                append(source ? session.getSourceEndpoint() : session.getDestinationEndpoint()).
                append(" -> ").
                append(source ? session.getDestinationEndpoint() : session.getSourceEndpoint()).
                append("]").
                toString();
    }

    @Override
    public void process(Selector selector) throws IOException {
        if (closed) {
            return;
        }

        int ops = key.readyOps();
        if ((ops & OP_CONNECT) != 0) {
            connected();
        }

        if ((ops & OP_READ) != 0) {
            receive();
        }

        if ((ops & OP_WRITE) != 0) {
            transmit();
        }
    }

    @Override
    public void terminate() {
        session.terminate();
    }

    public boolean isConnected() {
        return connected;
    }

    public void kill() {
        if (closed) {
            return;
        }

        while (!queue.isEmpty()) {
            allocator.reuse(queue.pop());
        }

        if (key != null) {
            key.cancel();
            key = null;
        }

        try {
            channel.close();
            closed = true;
            logger.info("{} killed", getId());
        } catch(IOException ioe) {
            logger.warn("{} kill failure {}", getId(), ioe.getMessage());
        }
    }

    private void enableTransmit(boolean enabled) {
        if (key == null) {
            return;
        }

        if (enabled) {
            key.interestOps(key.interestOps() | OP_WRITE);
        } else {
            key.interestOps(key.interestOps() & OP_READ);
        }

        logger.debug("{} transmit enabled: {}", getId(), enabled);
    }

    private void connected() throws IOException {
        session.active();

        try {
            channel.finishConnect();
            forwarder.getConnector().endpointConnected(endpoint);
            session.enableRead(!source, true);
            enableReceive(true);
            connected = true;
            logger.info("{} connected", getId());
        } catch(IOException ioe) {
            forwarder.getConnector().endpointUnavailable(endpoint);
            endpoint = endpoints.next();
            if (endpoint == null) {
                logger.info("{} connect failed, no more endpoints to try", getId());
                throw ioe;
            } else {
                key.cancel();
                channel.close();
                channel = endpoint.connect();
                this.key = channel.register(selector, OP_CONNECT, this);
                logger.info("{} connect failed, trying {}", getId(), endpoint);
                session.setDestinationEndpoint(endpoint);
            }
        }
    }

    private void receive() throws IOException {
        session.active();

        ByteBuffer buffer = allocator.create();
        int count = channel.read(buffer);

        logger.debug("{} read {} bytes", getId(), count);

        if (count < 0) {
            allocator.reuse(buffer);
            if (key != null) {
                key.cancel();
                key = null;
            }
            channel.close();
            closed = true;
            logger.info("{} closed", getId());
            session.closing(!source);
        } else {
            buffer.flip();
            session.append(!source, buffer);
        }
    }

    private void transmit() throws IOException {
        session.active();

        if (closing && !closed) {
            if (queue.isEmpty()) {
                if (key != null) {
                    key.cancel();
                    key = null;
                }
                channel.close();
                closed = true;
                logger.info("{} closed", getId());
                return;
            }
        }

        if (closed) {
            if (!queue.isEmpty()) {
                throw new ConnectionException("PeerClosedConnectionUnexpectedly");
            } else {
                // The processing of the READ on the channel has closed the connection.
                return;
            }
        }

        if (queue.isEmpty()) {
            logger.error("{} unexpected attempt to transmit", getId());
            return;
        }

        ByteBuffer next = queue.pop();

        logger.debug("{} sending {} bytes", getId(), next.remaining());

        channel.write(next);

        if (next.hasRemaining()) {
            logger.debug("{} re-queueing {} bytes", getId(), next.remaining());
            queue.head(next);
        } else {
            allocator.reuse(next);
        }

        logger.debug("{} queue is empty: {}", getId(), queue.isEmpty());

        enableReceive(queue.hasCapacity());
        enableTransmit(!queue.isEmpty() || closing);
    }
}