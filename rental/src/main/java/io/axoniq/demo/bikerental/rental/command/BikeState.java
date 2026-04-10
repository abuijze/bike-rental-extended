package io.axoniq.demo.bikerental.rental.command;

import io.axoniq.demo.bikerental.coreapi.rental.BikeRegisteredEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeReturnedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.extension.spring.stereotype.EventSourced;

@EventSourced(tagKey = "Bike")
public class BikeState {

    private boolean available;

    @EntityCreator
    public BikeState(BikeRegisteredEvent event) {
        this.available = true;
    }

    @EventSourcingHandler
    protected void on(BikeRequestedEvent event) {
        this.available = false;
    }

    @EventSourcingHandler
    protected void on(RequestRejectedEvent event) {
        this.available = true;
    }

    @EventSourcingHandler
    protected void on(BikeReturnedEvent event) {
        this.available = true;
    }

    public boolean isAvailable() {
        return available;
    }
}
