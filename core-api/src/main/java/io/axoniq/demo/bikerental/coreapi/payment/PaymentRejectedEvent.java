package io.axoniq.demo.bikerental.coreapi.payment;

import org.axonframework.eventsourcing.annotation.EventTag;

public record PaymentRejectedEvent(@EventTag(key = "Payment") String paymentId, String paymentReference) {
}
