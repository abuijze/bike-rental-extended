package io.axoniq.demo.bikerental.rental;

import org.axonframework.test.server.AxonServerContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestRentalApplication {

    public static void main(String[] args) {
        SpringApplication.from(RentalApplication::main)
                         .with(TestRentalApplication.class)
                         .run(args);
    }

    @Bean
    @ServiceConnection
    @RestartScope
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
    }

    @Bean
    @ServiceConnection
    @RestartScope
    AxonServerContainer axonServerContainer() {
        return new AxonServerContainer(DockerImageName.parse("axoniq/axonserver:latest-dev"));
    }

}