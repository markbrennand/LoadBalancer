package uk.org.shonky.loadbalancer.engine;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProcessorState {
    private String id;
    private int ops;
    private long expiry;

    public ProcessorState(String id, int ops, long expiry) {
        this.id = checkNotNull(id);
        this.ops = ops;
        this.expiry = expiry;
    }

    public String getId() {
        return id;
    }

    public int getOps() {
        return ops;
    }

    public long getExpiry() {
        return expiry;
    }
}
