package io.axoniq.demo.bikerental.rental.command;

import io.axoniq.demo.bikerental.coreapi.rental.*;
import org.axonframework.messaging.commandhandling.CommandExecutionException;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.modelling.annotation.InjectEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BikeCommandHandler {

    @CommandHandler
    public static void handle(RegisterBikeCommand command, EventAppender appender) {
        appender.append(new BikeRegisteredEvent(command.bikeId(), command.bikeType(), command.location()));
    }

    @CommandHandler
    public String handle(RequestBikeCommand command, @InjectEntity(idProperty = "bikeId") BikeState bike, EventAppender appender) {
        if (!bike.isAvailable()) {
            throw new CommandExecutionException("Bike is already rented", null, "Already rented");
        }
        String rentalReference = UUID.randomUUID().toString();
        appender.append(new BikeRequestedEvent(command.bikeId(), command.renter(), rentalReference));
        return rentalReference;
    }

    @CommandHandler
    public void handle(ApproveRequestCommand command, @InjectEntity(idProperty = "rentalReference") RentalState rental, EventAppender appender) {
        if (rental.isActive() && !rental.isConfirmed()) {
            appender.append(new BikeInUseEvent(rental.getBikeId(), rental.getRenter(), command.rentalReference()));
        }
    }

    @CommandHandler
    public void handle(RejectRequestCommand command, @InjectEntity(idProperty = "rentalReference") RentalState rental, EventAppender appender) {
        if (rental.isActive() && !rental.isConfirmed()) {
            appender.append(new RequestRejectedEvent(rental.getBikeId(), command.rentalReference()));
        }
    }

    @CommandHandler
    public void handle(ReturnBikeCommand command, @InjectEntity(idProperty = "rentalReference") RentalState rental, EventAppender appender) {
        if (!rental.isActive()) {
            throw new IllegalStateException("Bike was already returned");
        }
        appender.append(new BikeReturnedEvent(command.bikeId(), command.rentalReference(), command.location()));
    }
}
