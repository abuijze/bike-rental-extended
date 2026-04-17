package io.axoniq.demo.bikerental.rental.command;

import io.axoniq.demo.bikerental.coreapi.rental.*;
import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.commandhandling.CommandExecutionException;
import org.axonframework.messaging.commandhandling.configuration.CommandHandlingModule;
import org.axonframework.test.fixture.AxonTestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BikeTest {

    private AxonTestFixture fixture;

    @AfterEach
    void tearDown() {
        fixture.stop();
    }

    @BeforeEach
    void setUp() {
        fixture = AxonTestFixture.with(EventSourcingConfigurer.create()
                                                              .registerEntity(EventSourcedEntityModule.autodetected(String.class, BikeState.class))
                                                              .registerEntity(EventSourcedEntityModule.autodetected(String.class, RentalState.class))
                                                              .registerCommandHandlingModule(CommandHandlingModule.named("rental")
                                                                                                                  .commandHandlers()
                                                                                                                  .autodetectedCommandHandlingComponent(c -> new BikeCommandHandler())),
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
               .when()
               .command(new RequestBikeCommand("bikeId", "rider"))
                .then()
               .resultMessagePayloadSatisfies(String.class, i -> assertThat(i).isNotBlank())
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
               .eventsMatch(List::isEmpty);
    }

    @Test
    void canApproveRequestedBike() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when().command(new ApproveRequestCommand("rentalId"))
               .then()
               .success()
               .events(new BikeInUseEvent("bikeId", "rider", "rentalId"));
    }

    @Test
    void canRejectRequestedBike() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when().command(new RejectRequestCommand("rentalId"))
               .then()
               .success()
               .events(new RequestRejectedEvent("bikeId", "rentalId"));
    }

    @Test
    void canNotRejectWithUnknownRentalReference() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when().command(new RejectRequestCommand("wrongRentalId"))
               .then()
               .success()
               .eventsMatch(List::isEmpty);
    }

    @Test
    void cannotApproveWithUnknownRentalReference() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when().command(new ApproveRequestCommand("wrongRentalId"))
               .then()
               .success()
               .eventsMatch(List::isEmpty);
    }

    @Test
    void canReturnedBikeInUse() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                               new BikeInUseEvent("bikeId", "rider", "rentalId"))
               .when().command(new ReturnBikeCommand("bikeId", "rentalId", "NewLocation"))
               .then()
               .success()
               .events(new BikeReturnedEvent("bikeId", "rentalId", "NewLocation"));
    }

    @Test
    void cannotRequestBikeInUse() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                               new BikeInUseEvent("bikeId", "rider", "rentalId"))
               .when().command(new RequestBikeCommand("bikeId", "otherRenter"))
               .then()
               .exception(CommandExecutionException.class)
               .eventsMatch(List::isEmpty);
    }

    @Test
    void canRequestReturnedBike() {
        fixture.given().events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                               new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                               new BikeInUseEvent("bikeId", "rider", "rentalId"),
                               new BikeReturnedEvent("bikeId", "rentalId", "NewLocation"))
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
                               new RequestRejectedEvent("bikeId", "rentalId"))
               .when().command(new RequestBikeCommand("bikeId", "newRider"))
               .then()
               .success()
               .eventsMatch(l -> l.size() == 1
                               && l.getFirst().payloadAs(BikeRequestedEvent.class).bikeId().equals("bikeId")
                               && l.getFirst().payloadAs(BikeRequestedEvent.class).renter().equals("newRider"));
    }
}
