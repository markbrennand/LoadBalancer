package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import uk.org.shonky.loadbalancer.util.Allocator;
import uk.org.shonky.loadbalancer.util.DeliveryQueue;

import static com.google.common.base.Preconditions.checkNotNull;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static java.nio.channels.SelectionKey.OP_CONNECT;

public class Connection implements Processor {
    private static final Logger logger = Logger.getLogger(Connection.class);

    private Session session;
    private boolean source;
    private DeliveryQueue<ByteBuffer> queue;
    private Allocator<ByteBuffer> allocator;
    private SocketChannel channel;
    private SelectionKey key;
    private String tag;
    private boolean closing;
    private boolean closed;

    public Connection(String tag, Session session, boolean source, Selector selector, SocketChannel channe,
                      int maxQueueSize, Allocator<ByteBuffer> allocator)
            throws IOException
    {
        if (logger.isTraceEnabled()) {
            logger.trace("Creating connection with tag '" + tag + "', channel '" + channel + "'");
        }
        this.tag = checkNotNull(tag);
        this.allocator= checkNotNull(allocator);
        this.session = session;
        this.source = source;
        this.channel = checkNotNull(channel);
        this.queue = new DeliveryQueue<ByteBuffer>(maxQueueSize);

        if (channel.isConnected()) {
            this.key = channel.register(selector, OP_READ, this);
        } else {
            this.key = channel.register(selector, OP_CONNECT, this);
        }

        if (logger.isInfoEnabled()) {
            logger.info(tag + " registered with " + selector + ", channel connected: " + channel.isConnected());
        }
    }

    public void append(ByteBuffer buffer) {
        if (logger.isTraceEnabled()) {
            logger.trace(tag + " queueing buffer of " + buffer.remaining() + " bytes");
        }
        queue.append(buffer);
        session.enableRead(!source, queue.hasCapacity());
        enableTransmit(true);
    }

    public void enableReceive(boolean enabled) {
        if (enabled) {
            key.interestOps(key.interestOps() | OP_READ);
        } else {
            key.interestOps(key.interestOps() & OP_WRITE);
        }
    }

    public void close() {
        closing = true;
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
        }

        try {
            channel.close();
            if (logger.isInfoEnabled()) {
                logger.info(tag + " channel terminated");
            }
        } catch(IOException ioe) {
            logger.warn(tag + " termination failure", ioe);
        }
    }

    @Override
    public Session process(Selector selector, SocketChannel channel) throws IOException{
        if (key.isReadable()) {
            receive();
        }

        if (key.isWritable()) {
            transmit();
        }

        if (key.isConnectable()) {
            connected();
        }

        return session;
    }

    private void connected() throws IOException {
        channel.finishConnect();
        key.interestOps(OP_READ);
    }

    private void receive() throws IOException {
        ByteBuffer buffer = allocator.create();
        int count = channel.read(buffer);

        if (logger.isTraceEnabled()) {
            logger.trace(tag + " read " + count + " bytes");
        }

        if (count < 0) {
            allocator.reuse(buffer);
            if (key != null) {
                key.cancel();
            }
            channel.close();
            closed = true;
            session.closing(!source);
        } else {
            session.append(!source, buffer);
        }
    }

    private void enableTransmit(boolean enabled) {
        if (enabled) {
            key.interestOps(key.interestOps() | OP_WRITE);
        } else {
            key.interestOps(key.interestOps() & OP_READ);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(tag + " transmit enabled: " + enabled);
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
            logger.error(tag + " unexpected attempt to transmit");
            return;
        }

        ByteBuffer next = queue.pop();

        if (logger.isInfoEnabled()) {
            logger.info(tag + " sending " + next.remaining() + " bytes");
        }

        channel.write(next);

        if (next.hasRemaining()) {
            if (logger.isInfoEnabled()) {
                logger.info(tag + " re-queueing " + next.remaining() + " bytes");
            }
            queue.head(next);
        } else {
            allocator.reuse(next);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(tag + " queue is empty: " + queue.isEmpty());
        }

        enableReceive(queue.isEmpty());
        enableTransmit(!queue.isEmpty() || closing);
    }
}