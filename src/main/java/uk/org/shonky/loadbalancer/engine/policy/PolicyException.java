package uk.org.shonky.loadbalancer.engine.policy;

import uk.org.shonky.loadbalancer.util.BaseException;

public class PolicyException extends BaseException {

    public PolicyException(String message) {
        super(message);
    }

    public PolicyException(String message, Object... args) {
        super(message, args);
    }
}
