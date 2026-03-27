package io.axoniq.demo.bikerental.coreapi.payment;

import org.axonframework.messaging.queryhandling.annotation.Query;

@Query
public record GetPaymentIdQuery(String paymentReference) {
}
