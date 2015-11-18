import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import uk.org.shonky.loadbalancer.dao.MonitoringDAO;
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
        MonitoringDAO monitoringDAO = context.getBeanFactory().getBean(MonitoringDAO.class);

        ProcessorThread processor = new ProcessorThread();
        monitoringDAO.addProcessorThread(processor);

        List<Service> services = configService.getServices();
        for (Service service : services) {
            Listener listener = new Listener(service, 16, new SimpleByteBufferAllocator());
            processor.addListener(listener);
        }

        new Thread(processor).start();
    }
}
