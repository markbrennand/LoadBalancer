import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import uk.org.shonky.loadbalancer.engine.ProcessorState;
import uk.org.shonky.loadbalancer.engine.ProcessorThread;
import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.net.Listener;
import uk.org.shonky.loadbalancer.util.impl.SimpleByteBufferAllocator;
import uk.org.shonky.loadbalancer.services.ConfigurationService;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

@SpringBootApplication
@ComponentScan(value = { "uk.org.shonky.loadbalancer" })
public class DevelopmentTest {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(DevelopmentTest.class, args);
        ConfigurationService configService = context.getBeanFactory().getBean(ConfigurationService.class);

        ProcessorThread processor = new ProcessorThread();

        List<Service> services = configService.getServices();
        for (Service service : services) {
            Listener listener = new Listener(service, 16, new SimpleByteBufferAllocator());
            processor.addListener(listener);
        }

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
