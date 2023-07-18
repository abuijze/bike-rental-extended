package io.axoniq.demo.bikerental.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import jakarta.persistence.EntityManager;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.config.Configuration;
import org.axonframework.config.ConfigurerModule;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EntityScan(basePackageClasses = {PaymentStatus.class, TokenEntry.class})
@SpringBootApplication
@ImportRuntimeHints(CustomRuntimeHints.class)
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService workerExecutorService() {
        return Executors.newScheduledThreadPool(4);
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
                        "io.axoniq.demo.bikerental.payment",
                        Configuration::eventStore,
                        (c, b) -> b.workerExecutor(workerExecutorService())
                                   .batchSize(100)
                );
    }

}
