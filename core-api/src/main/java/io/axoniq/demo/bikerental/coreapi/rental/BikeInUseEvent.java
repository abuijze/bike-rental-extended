package io.axoniq.demo.bikerental.coreapi.rental;

import org.axonframework.eventsourcing.annotation.EventTag;
import org.axonframework.messaging.eventhandling.annotation.Event;

@Event
public record BikeInUseEvent(@EventTag(key = "Bike") String bikeId, String renter, @EventTag(key = "Rental") String rentalReference) {
}
