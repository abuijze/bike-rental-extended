package io.axoniq.demo.bikerental.coreapi.rental;

import org.axonframework.messaging.commandhandling.annotation.Command;
import org.axonframework.modelling.annotation.TargetEntityId;

@Command(routingKey = "bikeId")
public record ReturnBikeCommand(@TargetEntityId String bikeId, String location) {
}
