package io.axoniq.demo.bikerental.coreapi.payment;

import org.axonframework.eventsourcing.annotation.EventTag;

public record PaymentPreparedEvent(@EventTag(key = "Payment") String paymentId, int amount, String paymentReference) {
}
