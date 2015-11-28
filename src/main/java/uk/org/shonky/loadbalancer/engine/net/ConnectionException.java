package uk.org.shonky.loadbalancer.engine.net;

import uk.org.shonky.loadbalancer.util.BaseException;

public class ConnectionException extends BaseException {

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Object... args) {
        super(message, args);
    }
}
