package com.nanthealth.uk.tools.loadbalancer.util;

public class DeliveryQueue<T> {
    private T[] queue;
    private int allocated;
    private int max;

    public DeliveryQueue(int max) {
        this.queue = (T[]) new Object[max];
        this.max = max;
    }

    public T pop() {
        if (allocated == 0) {
            throw new DeliveryQueueEmptyException();
        }

        T value = queue[0];
        for (int i = 1; i < allocated; i++) {
            queue[i-1] = queue[i];
        }
        allocated--;
        return value;
    }

    public void append(T value) {
        if (allocated == max) {
            throw new DeliveryQueueFullException();
        }
        queue[allocated++] = value;
    }

    public void head(T value) {
        if (allocated == max) {
            throw new DeliveryQueueFullException();
        }
        for (int i = allocated -1; i >= 0; i--) {
            queue[i+1] = queue[i];
        }
        queue[0] = value;
        allocated++;
    }

    public boolean isEmpty() {
        return allocated == 0;
    }

    public boolean hasCapacity() {
        return allocated < max;
    }
}