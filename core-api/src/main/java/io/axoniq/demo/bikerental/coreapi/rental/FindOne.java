package io.axoniq.demo.bikerental.coreapi.rental;

import org.axonframework.messaging.queryhandling.annotation.Query;

@Query
public record FindOne(String bikeId) {
}
