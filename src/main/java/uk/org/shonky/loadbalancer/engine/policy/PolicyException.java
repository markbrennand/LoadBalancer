package uk.org.shonky.loadbalancer.engine.policy;

public class PolicyException extends RuntimeException {

    public PolicyException(String message) {
        super(message);
    }
}
