package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.shonky.loadbalancer.util.Allocator;
import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.policy.Connector;

import static java.nio.channels.SelectionKey.OP_ACCEPT;

import static com.google.common.base.Preconditions.checkNotNull;

public class Listener implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    private Forwarder forwarder;
    private ServerSocketChannel channel;
    private Endpoint endpoint;
    private Connector connector;
    private Allocator<ByteBuffer> allocator;
    private SelectionKey key;
    private int maxQueueSize;

    public Listener(Forwarder forwarder, int maxQueueSize,Allocator<ByteBuffer> allocator) throws IOException {
        this.forwarder = checkNotNull(forwarder);
        logger.info("Creating listener for {}", forwarder.getListeningEndpoint());
        this.connector = checkNotNull(forwarder.getConnector());
        channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        endpoint = forwarder.getListeningEndpoint();
        channel.bind(endpoint.getAddress());
        this.maxQueueSize = maxQueueSize;
        this.allocator = checkNotNull(allocator);
    }

    public void register(Selector selector) throws IOException {
        logger.debug("Registering channel with selector {}", selector);
        this.key = channel.register(checkNotNull(selector), OP_ACCEPT, this);
    }

    public void unregister() {
        if (key != null) {
            key.cancel();
            key = null;
        }
    }

    @Override
    public String getId() {
        return new StringBuffer(forwarder.getName()).append(" [Listener]").toString();
    }

    @Override
    public long getExpiry() {
        return Long.MAX_VALUE;
    }

    @Override
    public void process(Selector selector) throws IOException {
        if (key.isAcceptable()) {
            SocketChannel socketChannel = channel.accept();
            socketChannel.configureBlocking(false);
            new Session(forwarder, endpoint, socketChannel, selector, maxQueueSize, allocator);
        } else {
            throw new ConnectionException("ListenerUnexpectedOperation");
        }
    }

    @Override
    public void terminate() {
        try {
            channel.close();
        } catch(IOException ioe) {
            logger.error("{} termination failure  {}", channel, ioe.getMessage());
        }
    }
}