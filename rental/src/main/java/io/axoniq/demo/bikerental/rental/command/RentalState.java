package io.axoniq.demo.bikerental.rental.command;

import io.axoniq.demo.bikerental.coreapi.rental.BikeInUseEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeReturnedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.extension.spring.stereotype.EventSourced;

@EventSourced(tagKey = "Rental")
public class RentalState {

    private String bikeId;
    private String renter;
    private boolean confirmed;
    private boolean active;

    @EntityCreator
    public RentalState() {
    }

    @EventSourcingHandler
    protected void on(BikeRequestedEvent event) {
        this.bikeId = event.bikeId();
        this.renter = event.renter();
        this.active = true;
    }

    @EventSourcingHandler
    protected void on(BikeInUseEvent event) {
        this.confirmed = true;
    }

    @EventSourcingHandler
    protected void on(RequestRejectedEvent event) {
        this.active = false;
    }

    @EventSourcingHandler
    protected void on(BikeReturnedEvent event) {
        this.active = false;
    }

    public String getBikeId() {
        return bikeId;
    }

    public String getRenter() {
        return renter;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isActive() {
        return active;
    }
}
