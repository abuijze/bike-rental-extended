package io.axoniq.demo.bikerental.rental.command;

import io.axoniq.demo.bikerental.coreapi.rental.*;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.extension.spring.stereotype.EventSourced;
import org.axonframework.messaging.commandhandling.CommandExecutionException;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;

import java.util.Objects;
import java.util.UUID;

@EventSourced(tagKey = "Bike")
public class Bike {

    private final String bikeId;

    private boolean isAvailable;
    private String reservedBy;
    private boolean reservationConfirmed;

    @EntityCreator
    public Bike(BikeRegisteredEvent event) {
        this.bikeId = event.bikeId();
        this.isAvailable = true;
    }

    @CommandHandler
    public static void handle(RegisterBikeCommand command, EventAppender appender) {
        appender.append(new BikeRegisteredEvent(command.bikeId(), command.bikeType(), command.location()));
    }

    @CommandHandler
    public String handle(RequestBikeCommand command, EventAppender appender) {
        if (!this.isAvailable) {
            throw new CommandExecutionException("Bike is already rented", null, "Already rented");
        }
        String rentalReference = UUID.randomUUID().toString();
        appender.append(new BikeRequestedEvent(command.bikeId(), command.renter(), rentalReference));

        return rentalReference;
    }

    @CommandHandler
    public void handle(ApproveRequestCommand command, EventAppender appender) {
        if (!Objects.equals(reservedBy, command.renter())
                || reservationConfirmed) {
            return;
        }
        appender.append(new BikeInUseEvent(command.bikeId(), command.renter()));
    }

    @CommandHandler
    public void handle(RejectRequestCommand command, EventAppender appender) {
        if (!Objects.equals(reservedBy, command.renter())
                || reservationConfirmed) {
            return;
        }
        appender.append(new RequestRejectedEvent(command.bikeId()));
    }

    @CommandHandler
    public void handle(ReturnBikeCommand command, EventAppender appender) {
        if (this.isAvailable) {
            throw new IllegalStateException("Bike was already returned");
        }
        appender.append(new BikeReturnedEvent(command.bikeId(), command.location()));
    }

    @EventSourcingHandler
    protected void handle(BikeReturnedEvent event) {
        this.isAvailable = true;
        this.reservationConfirmed = false;
        this.reservedBy = null;
    }

    @EventSourcingHandler
    protected void handle(BikeRequestedEvent event) {
        this.reservedBy = event.renter();
        this.reservationConfirmed = false;
        this.isAvailable = false;
    }

    @EventSourcingHandler
    protected void handle(RequestRejectedEvent event) {
        this.reservedBy = null;
        this.reservationConfirmed = false;
        this.isAvailable = true;
    }

    @EventSourcingHandler
    protected void on(BikeInUseEvent event) {
        this.isAvailable = false;
        this.reservationConfirmed = true;
    }

    // getters for Jackson / JSON Serialization

    @SuppressWarnings("unused")
    public String getBikeId() {
        return bikeId;
    }

    @SuppressWarnings("unused")
    public boolean isAvailable() {
        return isAvailable;
    }

    @SuppressWarnings("unused")
    public String getReservedBy() {
        return reservedBy;
    }

    @SuppressWarnings("unused")
    public boolean isReservationConfirmed() {
        return reservationConfirmed;
    }
}
