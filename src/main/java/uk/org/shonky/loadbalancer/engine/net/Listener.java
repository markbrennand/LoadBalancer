package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import org.apache.log4j.Logger;

import uk.org.shonky.loadbalancer.engine.config.Connector;

import static java.nio.channels.SelectionKey.OP_ACCEPT;

import static com.google.common.base.Preconditions.checkNotNull;

public class Listener {
    private static final Logger logger = Logger.getLogger(Listener.class);

    private ServerSocketChannel channel;
    private Connector policy;
    private SelectionKey key;

    public Listener(InetAddress address, int port, Connector policy) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("Creating listener for " + address.getHostAddress() + ", port " + port + ", with policy " +
                    policy);
        }
        this.policy = checkNotNull(policy);
        channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(address, port));
    }

    public void register(Selector selector) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("Registering channel with selector " + selector);
        }
        this.key = channel.register(checkNotNull(selector), OP_ACCEPT, policy);
    }

    public void unregister() {
        if (key != null) {
            key.cancel();
            key = null;
        }
    }
}
