package uk.org.shonky.loadbalancer.util;

import java.text.MessageFormat;

public class BaseException extends RuntimeException {

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Object... args) {
        super(new MessageFormat(message).format(args));
    }
}