package uk.org.shonky.loadbalancer.engine.net;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.OP_READ;

public class Session {
    private Connection sourceConnection;
    private Connection destinationConnection;

    public Session(SocketChannel source, SocketChannel destination, Selector selector,int maxQueueSize)
            throws IOException
    {

        Socket sourceSocket = source.socket();
        Socket destinationSocket = destination.socket();

        String from = new StringBuffer(sourceSocket.getInetAddress().getHostAddress()).
                append("(").
                append(sourceSocket.getLocalPort()).
                append(")").
                toString();

        String to = new StringBuffer(destinationSocket.getInetAddress().getHostAddress()).
                append("(").
                append(destinationSocket.getPort()).
                append(")").
                toString();

        sourceConnection = new Connection(
                new StringBuffer(from).append(" -> ").append(to).toString(),
                destination,
                maxQueueSize);

        destinationConnection = new Connection(
                new StringBuffer(from).append(" <- ").append(to).toString(),
                source,
                maxQueueSize);

        source.register(selector, OP_READ, destinationConnection);
        destination.register(selector, OP_READ, sourceConnection);
        sourceConnection.register(selector);
        destinationConnection.register(selector);
    }

    public void close() throws IOException{
        sourceConnection.close();
        destinationConnection.close();
    }

    public void terminate() {
        sourceConnection.terminate();
        destinationConnection.terminate();
    }
}
