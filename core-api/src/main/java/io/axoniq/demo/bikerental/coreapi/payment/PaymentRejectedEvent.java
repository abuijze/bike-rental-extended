package io.axoniq.demo.bikerental.coreapi.payment;

import org.axonframework.eventsourcing.annotation.EventTag;
import org.axonframework.messaging.eventhandling.annotation.Event;

@Event
public record PaymentRejectedEvent(@EventTag(key = "Payment") String paymentId, String paymentReference) {
}
