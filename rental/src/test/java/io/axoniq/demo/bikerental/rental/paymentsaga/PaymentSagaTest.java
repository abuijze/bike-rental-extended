package io.axoniq.demo.bikerental.rental.paymentsaga;

import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentPreparedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand;
import io.axoniq.demo.bikerental.coreapi.payment.RejectPaymentCommand;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

class PaymentSagaTest {

    private SagaTestFixture fixture;

    @BeforeEach
    void setUp() {
        fixture = new SagaTestFixture(PaymentSaga.class);
    }

    @Test
    void shouldStartSagaOnBikeRequested() {
        fixture.givenNoPriorActivity()
               .whenPublishingA(new BikeRequestedEvent("bikeId", "renter", "payRef"))
               .expectDispatchedCommands(new PreparePaymentCommand(10, "payRef"))
               .expectActiveSagas(1);
    }

    @Test
    void shouldAcceptRequestOnPaymentConfirmed() {
        fixture.givenAPublished(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
               .whenPublishingA(new PaymentConfirmedEvent("paymentId", "rentalRef"))
               .expectDispatchedCommands(new ApproveRequestCommand("bikeId", "renter"))
               .expectActiveSagas(0);
    }

    @Test
    void shouldRejectRequestOnPaymentRejected() {
        fixture.givenAPublished(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
               .whenPublishingA(new PaymentRejectedEvent("paymentId", "rentalRef"))
               .expectDispatchedCommands(new RejectRequestCommand("bikeId", "renter"));
    }

    @Test
    void shouldEndSagaWhenRequestIsRejected() {
        fixture.givenAPublished(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
                .whenPublishingA(new RequestRejectedEvent("bikeId"))
                .expectActiveSagas(0);

    }

    @Test
    void shouldRejectPaymentWhenNotConfirmedIn30Seconds() {
        fixture.givenAPublished(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
                .andThenAPublished(new PaymentPreparedEvent("paymentId", 10, "rentalRef"))
                .whenTimeElapses(Duration.ofSeconds(30))
                .expectDispatchedCommands(new RejectPaymentCommand("paymentId"));

    }

}