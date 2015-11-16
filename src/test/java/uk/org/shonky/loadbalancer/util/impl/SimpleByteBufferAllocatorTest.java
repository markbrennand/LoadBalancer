package uk.org.shonky.loadbalancer.util.impl;

import java.nio.ByteBuffer;

import org.junit.*;
import uk.org.shonky.loadbalancer.util.impl.SimpleByteBufferAllocator;

import static org.junit.Assert.*;

public class SimpleByteBufferAllocatorTest {

    @Test
    public void testDefaultAllocator() {
        SimpleByteBufferAllocator allocator = new SimpleByteBufferAllocator();
        assertEquals(1024, allocator.getBufferSize());
        ByteBuffer buffer = allocator.create();
        assertEquals(1024, buffer.remaining());
        allocator.reuse(buffer);
    }

    @Test
    public void testFixedSizeAllocator() {
        SimpleByteBufferAllocator allocator = new SimpleByteBufferAllocator();
        allocator.setBufferSize(2048);
        assertEquals(2048, allocator.getBufferSize());
        ByteBuffer buffer = allocator.create();
        assertEquals(2048, buffer.remaining());
        allocator.reuse(buffer);
    }
}
