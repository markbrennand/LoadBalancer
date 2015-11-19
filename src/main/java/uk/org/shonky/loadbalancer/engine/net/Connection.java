package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.util.Allocator;
import uk.org.shonky.loadbalancer.util.DeliveryQueue;
import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Endpoints;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static java.nio.channels.SelectionKey.OP_CONNECT;

import static com.google.common.base.Preconditions.checkNotNull;

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
    private boolean closing;
    private boolean closed;

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
        this.key = this.channel.register(selector, OP_READ, this);
        this.source = true;
        session.active();

        logger.info("{} source connection registered with selector {}", getId(), selector);
    }

    public Connection(Forwarder forwarder, Session session, Selector selector, int maxQueueSize,
                      Allocator<ByteBuffer> allocator)
            throws IOException
    {
        this.forwarder = checkNotNull(forwarder);
        this.session = checkNotNull(session);
        this.selector = checkNotNull(selector);
        this.endpoints = forwarder.getConnector().nextConnectionEndpoints();
        this.endpoint = endpoints.next();
        this.channel = this.endpoint.connect();
        this.allocator = checkNotNull(allocator);
        this.queue = new DeliveryQueue<ByteBuffer>(maxQueueSize);
        this.key = this.channel.register(selector, OP_CONNECT, this);
        session.setDestinationEndpoint(endpoint);
        this.source = false;

        logger.info("{} connection initiated", getId());
        logger.info("{} registered with selector {}", getId(), selector);
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
            key.interestOps(key.interestOps() | OP_READ);
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
                append(session.getSourceEndpoint()).
                append(source ? " -> " : " <- ").
                append(session.getDestinationEndpoint()).
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
            key.interestOps(OP_READ);
            logger.info("{} connected", getId());
        } catch(IOException ioe) {
            endpoint = endpoints.next();
            if (endpoint == null) {
                logger.info("{} connect failed, no more endpoints to try", getId(), endpoint);
                throw ioe;
            } else {
                key.cancel();
                channel.close();
                channel = endpoint.connect();
                this.key = channel.register(selector, OP_CONNECT, this);
                logger.info("{} connect failed, trying {}", getId(), endpoint);
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
            throw new ConnectionException("Connection closed");
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