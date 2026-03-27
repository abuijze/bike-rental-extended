package io.axoniq.demo.bikerental.rental.command;

import io.axoniq.demo.bikerental.coreapi.rental.*;
import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.commandhandling.CommandExecutionException;
import org.axonframework.test.fixture.AxonTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BikeTest {

    private AxonTestFixture fixture;

    @BeforeEach
    void setUp() {
        fixture = AxonTestFixture.with(EventSourcingConfigurer.create()
                                                              .registerEntity(EventSourcedEntityModule.autodetected(String.class, Bike.class)),
                                       AxonTestFixture.Customization::disableAxonServer);
    }

    @Test
    void canRegisterBike() {
        fixture.given()
                .noPriorActivity()
                .when()
                .command(new RegisterBikeCommand("bikeId", "city", "Amsterdam"))
                .then()
                .success();
    }

    @Test
    void canRequestAvailableBike() {
        fixture.given().event(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"))
               .when().command(new RequestBikeCommand("bikeId", "rider"))
                .then()
               .resultMessageSatisfies(i -> assertThat(i).isInstanceOf(String.class))
               .eventsMatch(l -> l.size() == 1 && l.getFirst().payloadAs(BikeRequestedEvent.class)
                                                   .bikeId().equals("bikeId")
                                                                  && l.getFirst().payloadAs(BikeRequestedEvent.class).renter().equals("rider"));
    }

    @Test
    void cannotRequestAlreadyRequestedBike() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when().command(new RequestBikeCommand("bikeId", "rider"))
               .then()
               .exception(CommandExecutionException.class)
               .eventsMatch(l -> l.isEmpty());
    }

    @Test
    void canApproveRequestedBike() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when().command(new ApproveRequestCommand("bikeId", "rider"))
               .then()
               .success()
               .events(new BikeInUseEvent("bikeId", "rider"));
    }

    @Test
    void canRejectRequestedBike() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when().command(new RejectRequestCommand("bikeId", "rider"))
               .then()
               .success()
               .events(new RequestRejectedEvent("bikeId"));
    }

    @Test
    void canNotRejectRequestedForWrongRequester() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when().command(new RejectRequestCommand("bikeId", "otherRider"))
               .then()
               .success()
               .eventsMatch(l -> l.isEmpty());
    }

    @Test
    void cannotApproveRequestedForAnotherRider() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when().command(new ApproveRequestCommand("bikeId", "otherRider"))
               .then()
               .success()
               .eventsMatch(l -> l.isEmpty());
    }

    @Test
    void canReturnedBikeInUse() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                               new BikeInUseEvent("bikeId", "rider"))
               .when().command(new ReturnBikeCommand("bikeId", "NewLocation"))
               .then()
               .success()
               .events(new BikeReturnedEvent("bikeId", "NewLocation"));
    }

    @Test
    void cannotRequestBikeInUse() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                               new BikeInUseEvent("bikeId", "rider"))
               .when().command(new RequestBikeCommand("bikeId", "otherRenter"))
               .then()
               .exception(CommandExecutionException.class)
               .eventsMatch(l -> l.isEmpty());
    }

    @Test
    void canRequestReturnedBike() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                               new BikeInUseEvent("bikeId", "rider"),
                               new BikeReturnedEvent("bikeId", "NewLocation"))
               .when().command(new RequestBikeCommand("bikeId", "newRider"))
               .then()
               .success()
               .eventsMatch(l -> l.size() == 1
                               && l.getFirst().payloadAs(BikeRequestedEvent.class).bikeId().equals("bikeId")
                               && l.getFirst().payloadAs(BikeRequestedEvent.class).renter().equals("newRider"));
    }

    @Test
    void canRequestRejectedBike() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                               new RequestRejectedEvent("bikeId"))
               .when().command(new RequestBikeCommand("bikeId", "newRider"))
               .then()
               .success()
               .eventsMatch(l -> l.size() == 1
                               && l.getFirst().payloadAs(BikeRequestedEvent.class).bikeId().equals("bikeId")
                               && l.getFirst().payloadAs(BikeRequestedEvent.class).renter().equals("newRider"));
    }
}