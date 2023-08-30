package io.axoniq.demo.bikerental.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import jakarta.persistence.EntityManager;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.config.Configuration;
import org.axonframework.config.ConfigurerModule;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
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
@ImportRuntimeHints(PaymentApplication.CustomRuntimeHints.class)
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

    public static class CustomRuntimeHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.proxies().registerJdkProxy(TypeReference.of("org.hibernate.query.hql.spi.SqmQueryImplementor"),
                                             TypeReference.of("org.hibernate.query.sqm.internal.SqmInterpretationsKey$InterpretationsKeySource"),
                                             TypeReference.of("org.hibernate.query.spi.DomainQueryExecutionContext"),
                                             TypeReference.of("org.hibernate.query.SelectionQuery"),
                                             TypeReference.of("org.hibernate.query.CommonQueryContract"));
            hints.reflection()
                 .registerType(org.h2.server.TcpServer.class,
                               MemberCategory.PUBLIC_CLASSES,
                               MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                               MemberCategory.INVOKE_DECLARED_METHODS,
                               MemberCategory.INTROSPECT_DECLARED_METHODS,
                               MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS);

        }
    }
}
