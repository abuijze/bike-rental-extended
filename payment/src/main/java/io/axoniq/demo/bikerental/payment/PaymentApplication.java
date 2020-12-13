package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan(basePackageClasses = {PaymentStatus.class, TokenEntry.class})
@SpringBootApplication
public class PaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentApplication.class, args);
	}

	@Autowired
	public void configure(EventProcessingConfigurer config) {
		config.registerTrackingEventProcessor(
				"io.axoniq.demo.bikerental.payment",
				Configuration::eventStore,
				c -> TrackingEventProcessorConfiguration.forParallelProcessing(4)
														.andBatchSize(200));
	}

}
