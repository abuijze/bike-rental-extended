package io.axoniq.demo.bikerental.rental.paymentsaga;

import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import org.axonframework.test.fixture.AxonTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentSagaTest {

    private AxonTestFixture fixture;

    @BeforeEach
    void setUp() {
//        AxonTestFixture.with()
//        fixture = new SagaTestFixture(PaymentSaga.class);
    }

    @Test
    void shouldStartSagaOnBikeRequested() {
//        fixture.givenNoPriorActivity()
//               .whenPublishingA(new BikeRequestedEvent("bikeId", "renter", "payRef"))
//               .expectDispatchedCommands(new PreparePaymentCommand(10, "payRef"))
//               .expectActiveSagas(1);
    }

    @Test
    void shouldAcceptRequestOnPaymentConfirmed() {
        fixture.given()
               .events(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
               .when()
               .event(new PaymentConfirmedEvent("paymentId", "rentalRef"))
               .then()
               .commands(new ApproveRequestCommand("bikeId", "renter"));
    }

    @Test
    void shouldRejectRequestOnPaymentRejected() {
//        fixture.givenAPublished(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
//               .whenPublishingA(new PaymentRejectedEvent("paymentId", "rentalRef"))
//               .expectDispatchedCommands(new RejectRequestCommand("bikeId", "renter"));
    }

    @Test
    void shouldEndSagaWhenRequestIsRejected() {
//        fixture.givenAPublished(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
//                .whenPublishingA(new RequestRejectedEvent("bikeId"))
//                .expectActiveSagas(0);

    }

    @Test
    void shouldRejectPaymentWhenNotConfirmedIn30Seconds() {
//        fixture.givenAPublished(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
//                .andThenAPublished(new PaymentPreparedEvent("paymentId", 10, "rentalRef"))
//                .whenTimeElapses(Duration.ofSeconds(30))
//                .expectDispatchedCommands(new RejectPaymentCommand("paymentId"));

    }

}