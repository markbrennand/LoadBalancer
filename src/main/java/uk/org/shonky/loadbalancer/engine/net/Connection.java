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

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static java.nio.channels.SelectionKey.OP_CONNECT;

import static com.google.common.base.Preconditions.checkNotNull;

public class Connection implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    private Session session;
    private boolean source;
    private DeliveryQueue<ByteBuffer> queue;
    private Allocator<ByteBuffer> allocator;
    private SocketChannel channel;
    private SelectionKey key;
    private String id;
    private boolean closing;
    private boolean closed;

    public Connection(String id, Session session, boolean source, Selector selector, SocketChannel channel,
                      int maxQueueSize, Allocator<ByteBuffer> allocator)
            throws IOException
    {
        logger.debug("Creating connection with id {} and channel {}", id, this.channel);
        this.id = checkNotNull(id);
        this.session = checkNotNull(session);
        this.channel = checkNotNull(channel);
        this.allocator = checkNotNull(allocator);
        this.source = source;
        this.queue = new DeliveryQueue<ByteBuffer>(maxQueueSize);

        if (this.channel.isConnected()) {
            this.key = this.channel.register(selector, OP_READ, this);
        } else {
            this.key = this.channel.register(selector, OP_CONNECT, this);
        }

        logger.info("{} registered with selector {}, connected {}", id, selector, this.channel.isConnected());
    }

    public void append(ByteBuffer buffer) {
        logger.debug("{} queueing buffer of {} bytes", id, buffer.remaining());
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
        logger.info("{} closing", id);
        closing = true;
        enableTransmit(true);
    }

    @Override
    public long getExpiry() {
        return session.expiry();
    }

    @Override
    public String getId() {
        return id;
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

        return;
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
            logger.info("{} killed", id);
        } catch(IOException ioe) {
            logger.warn("{} kill failure {}", id, ioe.getMessage());
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

        logger.debug("{} transmit enabled: {}", id, enabled);
    }

    private void connected() throws IOException {
        session.active();

        channel.finishConnect();
        key.interestOps(OP_READ);
    }

    private void receive() throws IOException {
        session.active();

        ByteBuffer buffer = allocator.create();
        int count = channel.read(buffer);

        logger.debug("{} read {} bytes", id, count);

        if (count < 0) {
            allocator.reuse(buffer);
            if (key != null) {
                key.cancel();
                key = null;
            }
            channel.close();
            closed = true;
            logger.info("{} closed", id);
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
                logger.info("{} closed", id);
                return;
            }
        }

        if (closed) {
            throw new ConnectionException("Connection closed");
        }

        if (queue.isEmpty()) {
            logger.error("{} unexpected attempt to transmit", id);
            return;
        }

        ByteBuffer next = queue.pop();

        logger.debug("{} sending {} bytes", id, next.remaining());

        channel.write(next);

        if (next.hasRemaining()) {
            logger.debug("{} re-queueing {} bytes", id, next.remaining());
            queue.head(next);
        } else {
            allocator.reuse(next);
        }

        logger.debug("{} queue is empty: {}", id, queue.isEmpty());

        enableReceive(queue.hasCapacity());
        enableTransmit(!queue.isEmpty() || closing);
    }
}