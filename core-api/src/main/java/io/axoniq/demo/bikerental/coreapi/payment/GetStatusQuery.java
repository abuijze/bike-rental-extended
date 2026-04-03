package io.axoniq.demo.bikerental.coreapi.payment;


import org.axonframework.messaging.queryhandling.annotation.Query;

@Query(name = "getStatus")
public record GetStatusQuery(String paymentId) {
}
