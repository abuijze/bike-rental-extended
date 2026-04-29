package io.axoniq.demo.bikerental.rental;

import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import io.axoniq.demo.bikerental.rental.paymentsaga.PaymentSaga;
import io.axoniq.demo.bikerental.rental.paymentsaga.PaymentState;
import org.axonframework.extension.spring.config.EventProcessorDefinition;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.jpa.TokenEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EntityScan(basePackageClasses = {BikeStatus.class, PaymentState.class, TokenEntry.class})
@SpringBootApplication
public class RentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalApplication.class, args);
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService workerExecutorService() {
        return Executors.newScheduledThreadPool(4);
    }

    @Bean
    EventProcessorDefinition paymentSagaProcessor() {
        return EventProcessorDefinition.pooledStreaming("io.axoniq.demo.bikerental.rental.paymentsaga")
                .assigningHandlers(eh -> PaymentSaga.class.equals(eh.beanType()))
                .customized(p -> p.initialToken(ts -> ts.latestToken(null)));
    }

}
