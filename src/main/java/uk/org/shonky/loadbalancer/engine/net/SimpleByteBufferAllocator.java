package uk.org.shonky.loadbalancer.engine.net;

import java.nio.ByteBuffer;

import uk.org.shonky.loadbalancer.util.Allocator;

public class SimpleByteBufferAllocator implements Allocator<ByteBuffer> {
    private int bufferSize = 1024;

    @Override
    public ByteBuffer create() {
        return ByteBuffer.allocate(bufferSize);
    }

    @Override
    public void reuse(ByteBuffer value) {
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
