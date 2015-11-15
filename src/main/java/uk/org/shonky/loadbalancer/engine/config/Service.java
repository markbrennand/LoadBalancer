package uk.org.shonky.loadbalancer.engine.config;

import uk.org.shonky.loadbalancer.engine.net.Listener;

import static com.google.common.base.Preconditions.checkNotNull;

public class Service {
    private String name;
    private String description;
    private Listener listener;

    public Service(String name, String description, Listener listener) {
        this.name = checkNotNull(name);
        this.description = checkNotNull(description);
        this.listener = checkNotNull(listener);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Listener getListener() {
        return listener;
    }
}
