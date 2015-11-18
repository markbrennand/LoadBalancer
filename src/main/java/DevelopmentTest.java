import java.io.IOException;
import java.util.Properties;

import uk.org.shonky.loadbalancer.engine.ProcessorState;
import uk.org.shonky.loadbalancer.engine.ProcessorThread;
import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.config.PropertiesConfiguration;
import uk.org.shonky.loadbalancer.engine.net.Listener;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.engine.policy.impl.RoundRobinPolicy;
import uk.org.shonky.loadbalancer.util.impl.SimpleByteBufferAllocator;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class DevelopmentTest {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("TEST.service.getExpiry", "5000");
        props.put("TEST.service.listen.address", "7001");
        props.put("TEST.service.forward.addresses", "localhost:7002");
        ConnectorPolicy policy = new RoundRobinPolicy();
        Service service = new Service("TEST", new PropertiesConfiguration(props), policy);

        Listener listener = new Listener(service, 16, new SimpleByteBufferAllocator());

        ProcessorThread processor = new ProcessorThread();
        processor.addListener(listener);

        new Thread(processor).start();

        for (;;) {
            System.out.println("------");
            for (ProcessorState state : processor.getStates()) {
                System.out.print(state.getId() + ", expires " + state.getExpiry() +"ms, operations ");
                int ops = state.getOps();
                if ((ops & OP_ACCEPT) != 0) {
                    System.out.println("ACCEPT");
                } else if ((ops & OP_CONNECT) != 0) {
                    System.out.println("CONNECT");
                } else {
                    if ((ops & (OP_READ | OP_WRITE)) == (OP_READ | OP_WRITE)) {
                        System.out.println("READ+WRITE");
                    } else if ((ops & OP_READ) != 0) {
                        System.out.println("READ");
                    } else if ((ops & OP_WRITE) != 0) {
                        System.out.println("WRITE");
                    }
                }
            }
            Thread.sleep(1000);
        }
    }
}
