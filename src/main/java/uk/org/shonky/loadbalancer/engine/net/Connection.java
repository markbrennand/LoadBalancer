package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import uk.org.shonky.loadbalancer.util.DeliveryQueue;

import static java.nio.channels.SelectionKey.OP_WRITE;
import static java.nio.channels.SelectionKey.OP_CONNECT;

public class Connection {
    private DeliveryQueue<ByteBuffer> queue;
    private SocketChannel channel;
    private SelectionKey key;

    public Connection(SocketChannel channel, int maxQueueSize) {
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

    public void register(Selector selector) throws IOException {
        this.key = channel.register(selector, channel.isConnected() ? 0 : OP_CONNECT, this);
    }

    public void enableTransmit(boolean enable) {
        key.interestOps(enable ? OP_WRITE : 0);
    }

    public void close() throws IOException {
        channel.close();
    }
}
