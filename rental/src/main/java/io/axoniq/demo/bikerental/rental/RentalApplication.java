package io.axoniq.demo.bikerental.rental;

import com.thoughtworks.xstream.XStream;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.modelling.saga.repository.jpa.SagaEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EntityScan(basePackageClasses = {BikeStatus.class, SagaEntry.class, TokenEntry.class, PaymentStatus.class, DomainEventEntry.class})
@SpringBootApplication
public class RentalApplication {
    public static void main(String[] args) {
        SpringApplication.run(RentalApplication.class, args);
    }

    @Autowired
    public void configureXStreamSecurity(XStream xStream) {
        xStream.allowTypesByWildcard(new String[]{"io.axoniq.demo.bikerental.coreapi.**"});
    }

    @Bean
    public DeadlineManager deadlineManager(TransactionManager transactionManager,
                                           Configuration config) {
        return SimpleDeadlineManager.builder()
                                    .transactionManager(transactionManager)
                                    .scopeAwareProvider(new ConfigurationScopeAwareProvider(config))
                                    .build();
    }
}
