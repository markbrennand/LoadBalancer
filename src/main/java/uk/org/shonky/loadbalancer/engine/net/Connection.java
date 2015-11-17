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
    private String tag;
    private boolean closing;
    private boolean closed;

    public Connection(String tag, Session session, boolean source, Selector selector, SocketChannel channel,
                      int maxQueueSize, Allocator<ByteBuffer> allocator)
            throws IOException
    {
        logger.trace("Creating connection with tag '{}' and channel '{}'", tag, this.channel);
        this.tag = checkNotNull(tag);
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

        logger.info("{} registered with selector {}, connected {}", tag, selector, this.channel.isConnected());
    }

    public void append(ByteBuffer buffer) {
        logger.trace("{} queueing buffer of {} bytes", tag, buffer.remaining());
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
        closing = true;
        enableTransmit(true);
    }

    public void terminate() {
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
            logger.info("{} channel terminated", tag);
        } catch(IOException ioe) {
            logger.warn("{} termination failure '{}'", tag, ioe.getMessage());
        }
    }

    @Override
    public Session process(Selector selector) throws IOException {
        if (closed) {
            return session;
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

        return session;
    }

    private void connected() throws IOException {
        channel.finishConnect();
        key.interestOps(OP_READ);
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

        logger.trace(tag + " {} transmit enabled: {}", tag, enabled);
    }

    private void receive() throws IOException {
        ByteBuffer buffer = allocator.create();
        int count = channel.read(buffer);

        logger.trace("{} read {} bytes", tag, count);

        if (count < 0) {
            allocator.reuse(buffer);
            if (key != null) {
                key.cancel();
                key = null;
            }
            channel.close();
            closed = true;
            session.closing(!source);
        } else {
            buffer.flip();
            session.append(!source, buffer);
        }
    }

    private void transmit() throws IOException {
        if (closing) {
            if (logger.isTraceEnabled()) {
                logger.trace(tag + " closing");
            }
            if (queue.isEmpty()) {
                if (key != null) {
                    key.cancel();
                    key = null;
                }
                channel.close();
                closed = true;
                return;
            }
        }

        if (closed) {
            throw new ConnectionException("Stream closed");
        }

        if (queue.isEmpty()) {
            logger.error("{} unexpected attempt to transmit", tag);
            return;
        }

        ByteBuffer next = queue.pop();

        logger.debug("{} sending {} bytes", tag, next.remaining());

        channel.write(next);

        if (next.hasRemaining()) {
            logger.debug("{} re-queueing {} bytes", tag, next.remaining());
            queue.head(next);
        } else {
            allocator.reuse(next);
        }

        logger.trace("{} queue is empty: {}", tag, queue.isEmpty());

        enableReceive(queue.hasCapacity());
        enableTransmit(!queue.isEmpty() || closing);
    }
}