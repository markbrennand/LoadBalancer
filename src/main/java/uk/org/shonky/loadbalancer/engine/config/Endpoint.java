package uk.org.shonky.loadbalancer.engine.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import static com.google.common.base.Preconditions.checkNotNull;

public class Endpoint {
    private SocketAddress address;
    private int port;

    public Endpoint(InetAddress address, int port) {
        this.address = new InetSocketAddress(checkNotNull(address), port);
        this.port = port;
    }

    public SocketChannel connect() throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(address);
        return channel;
    }
}
