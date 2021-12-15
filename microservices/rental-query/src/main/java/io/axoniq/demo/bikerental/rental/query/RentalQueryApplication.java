package io.axoniq.demo.bikerental.rental.query;

import com.thoughtworks.xstream.XStream;
import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EntityScan(basePackageClasses = {BikeStatus.class, TokenEntry.class})
@SpringBootApplication
public class RentalQueryApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalQueryApplication.class, args);
	}

	@Bean(destroyMethod = "shutdown")
	public ScheduledExecutorService workerExecutorService() {
		return Executors.newScheduledThreadPool(2);
	}

	@Autowired
	public void configureXStreamSecurity(XStream xStream) {
		xStream.allowTypesByWildcard(new String[]{"io.axoniq.demo.bikerental.coreapi.**"});
	}

	@Autowired
	public void configure(EventProcessingConfigurer eventProcessing) {
		eventProcessing.usingPooledStreamingEventProcessors()
					   .registerPooledStreamingEventProcessorConfiguration(
							   (c, b) -> b.workerExecutorService(workerExecutorService())
					   );
	}
}
