package uk.org.shonky.loadbalancer.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Internationalisation {
    private static ResourceBundle bundle = ResourceBundle.getBundle("LoadBalancerMessages");

    public static String getMessage(String key, Object... args) {
        return new MessageFormat(bundle.getString(key)).format(args);
    }

    static void setBundle(ResourceBundle bundle) {
        Internationalisation.bundle = bundle;
    }
}
