package io.axoniq.demo.bikerental.rental.paymentsaga;

import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.modelling.saga.repository.jpa.SagaEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

@EntityScan(basePackageClasses = {BikeStatus.class, SagaEntry.class, TokenEntry.class})
@SpringBootApplication
public class RentalPaymentSagaApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalPaymentSagaApplication.class, args);
	}

	@Bean
	public DeadlineManager deadlineManager(TransactionManager transactionManager,
										   Configuration config) {
		return SimpleDeadlineManager.builder()
									.transactionManager(transactionManager)
									.scopeAwareProvider(new ConfigurationScopeAwareProvider(config))
									.build();
	}

	@Autowired
	public void configure(EventProcessingConfigurer eventProcessing) {
		eventProcessing.registerTrackingEventProcessor(
				"PaymentSagaProcessor",
				Configuration::eventStore,
				c -> TrackingEventProcessorConfiguration.forParallelProcessing(4)
														.andBatchSize(200)
														.andInitialTrackingToken(StreamableMessageSource::createHeadToken)
		);
	}

}
