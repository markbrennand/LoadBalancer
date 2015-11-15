package uk.org.shonky.loadbalancer.util;

public interface Allocator<T> {
    public T create();
    public void reuse(T value);
}
