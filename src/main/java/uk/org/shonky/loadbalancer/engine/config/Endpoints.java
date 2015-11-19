package uk.org.shonky.loadbalancer.engine.config;

import static com.google.common.base.Preconditions. checkNotNull;
import static com.google.common.base.Preconditions. checkElementIndex;

public class Endpoints {
    private Endpoint[] endpoints;
    private int max;
    private int first;
    private int last;
    private boolean wrapped;

    public Endpoints(Endpoint[] endpoints, int first, int last) {
        this.endpoints = checkNotNull(endpoints);
        this.max = endpoints.length;
        this.first = checkElementIndex(first, max);
        this.last = checkElementIndex(last, max);
        this.wrapped = last >= first;
    }

    public Endpoint next() {
        if (wrapped && first == (last+1)) {
            return null;
        }

        Endpoint next = endpoints[first++];
        if (first == max && ((last+1) != max)) {
            wrapped = true;
            first = 0;
        }

        return next;
    }
}
