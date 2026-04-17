package io.axoniq.demo.bikerental.coreapi.rental;

import org.axonframework.messaging.commandhandling.annotation.Command;

@Command(routingKey = "rentalReference")
public record ApproveRequestCommand(String rentalReference) {
}
