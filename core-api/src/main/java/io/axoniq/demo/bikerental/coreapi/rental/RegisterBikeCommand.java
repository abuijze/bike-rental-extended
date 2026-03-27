package io.axoniq.demo.bikerental.coreapi.rental;

import org.axonframework.messaging.commandhandling.annotation.Command;
import org.axonframework.modelling.annotation.TargetEntityId;

@Command(routingKey = "bikeId")
public record RegisterBikeCommand(@TargetEntityId String bikeId, String bikeType, String location) {
}
