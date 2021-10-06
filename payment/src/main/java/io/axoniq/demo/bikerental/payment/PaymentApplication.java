package io.axoniq.demo.bikerental.payment;

import com.thoughtworks.xstream.XStream;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EntityScan(basePackageClasses = {PaymentStatus.class, TokenEntry.class})
@SpringBootApplication
public class PaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentApplication.class, args);
	}

	@Bean(destroyMethod = "shutdown")
	public ScheduledExecutorService workerExecutorService() {
		return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	}

	@Autowired
	public void configureXStreamSecurity(XStream xStream) {
		xStream.allowTypesByWildcard(new String[]{"io.axoniq.demo.bikerental.coreapi.**"});
	}

	@Autowired
	public void configure(EventProcessingConfigurer config) {
		config.registerPooledStreamingEventProcessor(
				"io.axoniq.demo.bikerental.payment",
				Configuration::eventStore,
				(c, b) -> b.workerExecutor(workerExecutorService())
						   .batchSize(100)
		);
	}

}
