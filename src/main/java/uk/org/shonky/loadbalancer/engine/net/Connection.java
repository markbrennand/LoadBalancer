package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import uk.org.shonky.loadbalancer.util.DeliveryQueue;

import static com.google.common.base.Preconditions.checkNotNull;

import static java.nio.channels.SelectionKey.OP_WRITE;
import static java.nio.channels.SelectionKey.OP_CONNECT;

public class Connection {
    private static final Logger logger = Logger.getLogger(Connection.class);

    private DeliveryQueue<ByteBuffer> queue;
    private SocketChannel channel;
    private SelectionKey key;
    private String tag;

    public Connection(String tag, SocketChannel channel, int maxQueueSize) {
        if (logger.isTraceEnabled()) {
            logger.trace("Creating connection with tag '" + tag + "', channel '" + channel + "'");
        }
        this.tag = checkNotNull(tag);
        this.channel = checkNotNull(channel);
        this.queue = new DeliveryQueue<ByteBuffer>(maxQueueSize);
    }

    public boolean append(ByteBuffer buffer) {
        if (logger.isTraceEnabled()) {
            logger.trace(tag + " queueing buffer of " + buffer.remaining() + " bytes");
        }
        queue.append(buffer);
        return queue.hasCapacity();
    }

    public boolean transmit() throws IOException {
        if (queue.isEmpty()) {
            logger.error(tag + " unexpected attempt to transmit");
            return false;
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
        }

        if (logger.isTraceEnabled()) {
            logger.trace(tag + " queue is empty: " + queue.isEmpty());
        }

        return !queue.isEmpty();
    }

    public void register(Selector selector) throws IOException {
        this.key = channel.register(selector, channel.isConnected() ? 0 : OP_CONNECT, this);
        if (logger.isInfoEnabled()) {
            logger.info(tag + " registered with " + selector + ", channel connected: " + channel.isConnected());
        }
    }

    public void enableTransmit(boolean enable) {
        key.interestOps(enable ? OP_WRITE : 0);
        if (logger.isTraceEnabled()) {
            logger.trace(tag + " transmit enabled: " + enable);
        }
    }

    public void close() throws IOException {
        if (!queue.isEmpty()) {
            throw new ConnectionException("Connection cannot be closed until it has been flushed");
        }
        channel.close();
        if (logger.isInfoEnabled()) {
            logger.info(tag + " channel closed");
        }
    }

    public void terminate() {
        try {
            channel.close();
            if (logger.isInfoEnabled()) {
                logger.info(tag + " channel terminated");
            }
        } catch(IOException ioe) {
            logger.warn(tag + " termination failure", ioe);
        }
    }
}