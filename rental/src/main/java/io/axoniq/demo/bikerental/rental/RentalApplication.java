package io.axoniq.demo.bikerental.rental;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import jakarta.persistence.EntityManager;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.config.ConfigurerModule;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.modelling.saga.ResourceInjector;
import org.axonframework.modelling.saga.SimpleResourceInjector;
import org.axonframework.modelling.saga.repository.jpa.SagaEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EntityScan(basePackageClasses = {BikeStatus.class, SagaEntry.class, TokenEntry.class})
@SpringBootApplication
@ImportRuntimeHints(CustomRuntimeHints.class)
public class RentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalApplication.class, args);
    }

    @Bean(destroyMethod = "")
    public DeadlineManager deadlineManager(TransactionManager transactionManager,
                                           Configuration config) {
        return SimpleDeadlineManager.builder()
                                    .transactionManager(transactionManager)
                                    .scopeAwareProvider(new ConfigurationScopeAwareProvider(config))
                                    .build();
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService workerExecutorService() {
        return Executors.newScheduledThreadPool(4);
    }

    @Bean
    public ResourceInjector resourceInjector(CommandGateway commandGateway,
                                             DeadlineManager deadlineManager) {
        return new SimpleResourceInjector(commandGateway, deadlineManager);
    }

    @Bean
    public EntityManagerProvider entityManagerProvider(EntityManager entityManager) {
        return new SimpleEntityManagerProvider(entityManager);
    }

    @Autowired
    public void configureSerializers(ObjectMapper objectMapper) {
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
    }

    @Bean
    public ConfigurerModule eventProcessingCustomizer() {
        return configurer -> configurer
                .eventProcessing()
                .registerPooledStreamingEventProcessor(
                        "PaymentSagaProcessor",
                        Configuration::eventStore,
                        (c, b) -> b.workerExecutor(workerExecutorService())
                                   .initialSegmentCount(2)
                                   .batchSize(100)
                                   .initialToken(StreamableMessageSource::createHeadToken)
                )
                .registerPooledStreamingEventProcessor(
                        "io.axoniq.demo.bikerental.rental.query",
                        Configuration::eventStore,
                        (c, b) -> b.workerExecutor(workerExecutorService())
                                   .batchSize(100)
                );
    }

}
