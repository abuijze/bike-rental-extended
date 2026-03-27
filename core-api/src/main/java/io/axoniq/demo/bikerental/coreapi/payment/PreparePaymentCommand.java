package io.axoniq.demo.bikerental.coreapi.payment;

import org.axonframework.messaging.commandhandling.annotation.Command;

@Command(routingKey = "paymentReference")
public record PreparePaymentCommand(int amount, String paymentReference) {
}
