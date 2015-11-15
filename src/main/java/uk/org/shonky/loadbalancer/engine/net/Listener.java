package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import org.apache.log4j.Logger;

import uk.org.shonky.loadbalancer.util.Allocator;
import uk.org.shonky.loadbalancer.engine.config.Connector;

import static java.nio.channels.SelectionKey.OP_ACCEPT;

import static com.google.common.base.Preconditions.checkNotNull;

public class Listener {
    private static final Logger logger = Logger.getLogger(Listener.class);

    private ServerSocketChannel channel;
    private Connector connector;
    private Allocator<ByteBuffer> allocator;
    private SelectionKey key;
    private int maxQueueSize;

    public Listener(Connector connector, int maxQueueSize,Allocator<ByteBuffer> allocator) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("Creating listener for " + connector.getListeningAddress().getHostAddress() +
                    ", port " + connector.getListeningPort() + ", with connector " + connector);
        }
        this.connector = checkNotNull(connector);
        channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(checkNotNull(connector.getListeningAddress()), connector.getListeningPort()));
        this.maxQueueSize = maxQueueSize;
        this.allocator = allocator;
    }

    public Session accepted(SocketChannel channel, Selector selector) throws IOException {
        return new Session(channel, connector, selector, maxQueueSize, allocator);
    }

    public void register(Selector selector) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("Registering channel with selector " + selector);
        }
        this.key = channel.register(checkNotNull(selector), OP_ACCEPT, this);
    }

    public void unregister() {
        if (key != null) {
            key.cancel();
            key = null;
        }
    }
}