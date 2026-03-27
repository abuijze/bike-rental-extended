package io.axoniq.demo.bikerental.coreapi.payment;

import org.axonframework.eventsourcing.annotation.EventTag;

public record PaymentConfirmedEvent(@EventTag(key = "Payment") String paymentId, String paymentReference) {
}
