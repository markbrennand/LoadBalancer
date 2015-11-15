package uk.org.shonky.loadbalancer.engine.config;

import java.net.InetAddress;

public interface Connector {
    public int getListeningPort();
    public InetAddress getListeningAddress();
    public Endpoint nextEndpoint();
    public void endpointClosed(Endpoint endpoint);
}