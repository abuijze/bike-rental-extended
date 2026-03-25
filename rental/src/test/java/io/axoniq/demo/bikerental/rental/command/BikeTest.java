package io.axoniq.demo.bikerental.rental.command;

import org.axonframework.test.fixture.AxonTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BikeTest {

    private AxonTestFixture fixture;

    @BeforeEach
    void setUp() {
//        fixture = AxonTestFixture.with();
    }

    @Test
    void canRegisterBike() {
//        fixture.givenNoPriorActivity()
//               .when(new RegisterBikeCommand("bikeId", "city", "Amsterdam"))
//               .expectEvents(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"));
    }

    @Test
    void canRequestAvailableBike() {
//        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"))
//               .when(new RequestBikeCommand("bikeId", "rider"))
//               .expectResultMessagePayloadMatching(matches(String.class::isInstance))
//               .expectEventsMatching(exactSequenceOf(
//                       messageWithPayload(matches((BikeRequestedEvent e) ->
//                                                          e.getBikeId().equals("bikeId")
//                                                                  && e.getRenter().equals("rider"))),
//                       andNoMore()));
    }

    @Test
    void cannotRequestAlreadyRequestedBike() {
//        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
//                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
//               .when(new RequestBikeCommand("bikeId", "rider"))
//               .expectNoEvents()
//               .expectException(CommandExecutionException.class);
    }

    @Test
    void canApproveRequestedBike() {
//        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
//                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
//               .when(new ApproveRequestCommand("bikeId", "rider"))
//               .expectEvents(new BikeInUseEvent("bikeId", "rider"));
    }

    @Test
    void canRejectRequestedBike() {
//        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
//                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
//               .when(new RejectRequestCommand("bikeId", "rider"))
//               .expectEvents(new RequestRejectedEvent("bikeId"));
    }

    @Test
    void canNotRejectRequestedForWrongRequester() {
//        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
//                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
//               .when(new RejectRequestCommand("bikeId", "otherRider"))
//               .expectSuccessfulHandlerExecution()
//               .expectNoEvents();
    }

    @Test
    void cannotApproveRequestedForAnotherRider() {
//        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
//                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
//               .when(new ApproveRequestCommand("bikeId", "otherRider"))
//               .expectNoEvents()
//               .expectSuccessfulHandlerExecution();
    }

    @Test
    void canReturnedBikeInUse() {
//        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
//                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
//                      new BikeInUseEvent("bikeId", "rider"))
//               .when(new ReturnBikeCommand("bikeId", "NewLocation"))
//               .expectEvents(new BikeReturnedEvent("bikeId", "NewLocation"));
    }

    @Test
    void cannotRequestBikeInUse() {
//        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
//                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
//                      new BikeInUseEvent("bikeId", "rider"))
//               .when(new RequestBikeCommand("bikeId", "otherRenter"))
//               .expectNoEvents()
//               .expectException(CommandExecutionException.class);
    }

    @Test
    void canRequestReturnedBike() {
//        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
//                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
//                      new BikeInUseEvent("bikeId", "rider"),
//                      new BikeReturnedEvent("bikeId", "NewLocation"))
//               .when(new RequestBikeCommand("bikeId", "newRider"))
//               .expectEventsMatching(exactSequenceOf(
//                       messageWithPayload(matches((BikeRequestedEvent e) ->
//                                                          e.getBikeId().equals("bikeId")
//                                                                  && e.getRenter().equals("newRider"))),
//                       andNoMore()));
    }

    @Test
    void canRequestRejectedBike() {
//        fixture.given(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
//                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
//                      new RequestRejectedEvent("bikeId"))
//               .when(new RequestBikeCommand("bikeId", "newRider"))
//               .expectEventsMatching(exactSequenceOf(
//                       messageWithPayload(matches((BikeRequestedEvent e) ->
//                                                          e.getBikeId().equals("bikeId")
//                                                                  && e.getRenter().equals("newRider"))),
//                       andNoMore()));
    }
}