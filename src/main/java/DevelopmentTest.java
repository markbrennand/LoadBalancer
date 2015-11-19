import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import uk.org.shonky.loadbalancer.dao.MonitoringDAO;
import uk.org.shonky.loadbalancer.engine.ProcessorThread;
import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.net.Listener;
import uk.org.shonky.loadbalancer.util.impl.SimpleByteBufferAllocator;
import uk.org.shonky.loadbalancer.services.ConfigurationService;

@SpringBootApplication
@ComponentScan(value = {
        "uk.org.shonky.loadbalancer.dao.impl",
        "uk.org.shonky.loadbalancer.services.impl",
        "uk.org.shonky.loadbalancer.engine.policy.impl",
        "uk.org.shonky.loadbalancer.webservices"
})

public class DevelopmentTest {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(DevelopmentTest.class, args);
        ConfigurationService configService = context.getBeanFactory().getBean(ConfigurationService.class);
        MonitoringDAO monitoringDAO = context.getBeanFactory().getBean(MonitoringDAO.class);

        ProcessorThread processor = new ProcessorThread();
        monitoringDAO.addProcessorThread(processor);

        List<Forwarder> forwarders = configService.getForwarders();
        for (Forwarder forwarder : forwarders) {
            Listener listener = new Listener(forwarder, 16, new SimpleByteBufferAllocator());
            processor.addListener(listener);
        }

        new Thread(processor).start();
    }
}
