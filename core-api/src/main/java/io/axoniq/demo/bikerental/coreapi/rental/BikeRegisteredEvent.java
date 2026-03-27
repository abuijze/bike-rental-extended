package io.axoniq.demo.bikerental.coreapi.rental;

import org.axonframework.eventsourcing.annotation.EventTag;
import org.axonframework.messaging.eventhandling.annotation.Event;

@Event
public record BikeRegisteredEvent(@EventTag(key = "Bike") String bikeId, String bikeType, String location) {
}
