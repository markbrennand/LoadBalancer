import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.Iterator;
import java.util.Properties;

import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.config.PropertiesConfiguration;
import uk.org.shonky.loadbalancer.engine.net.Listener;
import uk.org.shonky.loadbalancer.engine.net.Processor;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.engine.policy.impl.RoundRobinPolicy;
import uk.org.shonky.loadbalancer.util.impl.SimpleByteBufferAllocator;

public class DevelopmentTest {

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.put("TEST.service.listen.address", "7001");
        props.put("TEST.service.forward.addresses", "172.20.13.152:80");
        ConnectorPolicy policy = new RoundRobinPolicy();
        Service service = new Service("TEST", new PropertiesConfiguration(props), policy);

        Listener listener = new Listener(service, 16, new SimpleByteBufferAllocator());

        Selector selector = Selector.open();
        listener.register(selector);

        while (true) {
            if (selector.select() > 0) {
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                while (iter.hasNext()) {
                    ((Processor) iter.next().attachment()).process(selector);
                    iter.remove();
                }
            }
        }
    }
}
