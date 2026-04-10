package io.axoniq.demo.bikerental.coreapi.rental;

import org.axonframework.eventsourcing.annotation.EventTag;

public record BikeReturnedEvent(@EventTag(key = "Bike") String bikeId, @EventTag(key = "Rental") String rentalReference, String location) {
}
