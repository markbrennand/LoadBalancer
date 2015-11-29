package uk.org.shonky.loadbalancer.util;

import java.text.MessageFormat;

public class BaseException extends RuntimeException {

    public BaseException(String key) {
        super(Internationalisation.getMessage(key));
    }

    public BaseException(String key, Object... args) {
        super(Internationalisation.getMessage(key, args));
    }
}