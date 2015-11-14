package com.nanthealth.uk.tools.loadbalancer.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import com.nanthealth.uk.tools.loadbalancer.util.DeliveryQueue;

public class Connection {
    private DeliveryQueue<ByteBuffer> queue;
    private SocketChannel channel;

    public Connection(SocketChannel channel, int maxQueueSize) {
        this.queue = queue;
        this.channel = channel;
        this.queue = new DeliveryQueue<ByteBuffer>(maxQueueSize);
    }

    public boolean append(ByteBuffer buffer) {
        queue.append(buffer);
        return queue.hasCapacity();
    }

    public boolean transmit() throws IOException {
        if (queue.isEmpty()) {
            return false;
        }

        ByteBuffer next = queue.pop();
        channel.write(next);

        if (next.hasRemaining()) {
            queue.head(next);
        }

        return !queue.isEmpty();
    }

    public void close() throws IOException {
        channel.close();
    }
}
