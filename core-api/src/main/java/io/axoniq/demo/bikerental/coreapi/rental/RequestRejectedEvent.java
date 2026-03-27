package io.axoniq.demo.bikerental.coreapi.rental;

import org.axonframework.eventsourcing.annotation.EventTag;

public record RequestRejectedEvent(@EventTag(key = "Bike") String bikeId) {
}
