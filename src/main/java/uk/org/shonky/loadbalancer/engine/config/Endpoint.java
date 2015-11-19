package uk.org.shonky.loadbalancer.engine.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class Endpoint {
    private final static Logger logger = LoggerFactory.getLogger(Endpoint.class);

    private InetSocketAddress address;

    public Endpoint(int port) {
       validatePort(port);
       this.address = new InetSocketAddress(port);
    }

    public Endpoint(InetAddress address, int port) {
        validatePort(port);
        this.address = new InetSocketAddress(checkNotNull(address), port);
    }

    public SocketChannel connect() throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(address);
        return channel;
    }

    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return new StringBuilder(address.getHostName()).append("(").append(address.getPort()).append(")").toString();
    }

    public static Endpoint parse(String value, boolean listeningAddress) {
        value = value.trim();
        String address;
        int split = value.indexOf(":");
        if (split < 1) {
            if (listeningAddress) {
                address = null;
            } else {
                throw new ConfigurationException("Invalid connection address ''{0}'', missing hostname / IP", value);
            }
        } else {
            address = value.substring(0, split);
        }

        if (split == (value.length() - 1)) {
            throw new ConfigurationException("Invalid address ''{0}'', missing port number", value);
        }

        try {
            if (address == null) {
                return new Endpoint(Integer.parseInt(value.substring(split + 1)));
            } else {
                return new Endpoint(InetAddress.getByName(address), Integer.parseInt(value.substring(split + 1)));
            }
        } catch(NumberFormatException nfe) {
            throw new ConfigurationException("Invalid address ''{0}'', non numeric port number", value);
        } catch(UnknownHostException uhe) {
            throw new ConfigurationException("Invalid address ''{0}'', hostname not found", value);
        }
    }

    private void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new ConfigurationException("Invalid port number, must be in range 1 .. 65535");
        }
    }
}
