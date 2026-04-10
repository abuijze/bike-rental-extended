package io.axoniq.demo.bikerental.coreapi.rental;

import org.axonframework.eventsourcing.annotation.EventTag;

public record BikeInUseEvent(@EventTag(key = "Bike") String bikeId, String renter, @EventTag(key = "Rental") String rentalReference) {
}
