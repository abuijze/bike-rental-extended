package io.axoniq.demo.bikerental.coreapi.payment;

import org.axonframework.eventsourcing.annotation.EventTag;
import org.axonframework.messaging.eventhandling.annotation.Event;

@Event
public record PaymentPreparedEvent(@EventTag(key = "Payment") String paymentId, int amount, String paymentReference) {
}
