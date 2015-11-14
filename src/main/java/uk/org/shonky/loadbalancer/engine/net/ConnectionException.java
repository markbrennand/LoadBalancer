package uk.org.shonky.loadbalancer.engine.net;

public class ConnectionException extends RuntimeException {

    public ConnectionException(String message) {
        super(message);
    }
}
