package io.axoniq.demo.bikerental.rental.paymentsaga;

import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand;
import io.axoniq.demo.bikerental.coreapi.payment.RejectPaymentCommand;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.core.configuration.MessagingConfigurer;
import org.axonframework.test.fixture.AxonTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentSagaTest {

    private PaymentStateRepository repository;
    private PaymentSaga paymentSaga;
    private AxonTestFixture fixture;
    private Map<String, PaymentState> stateStore;

    @BeforeEach
    void setUp() {
        stateStore = new HashMap<>();
        repository = mock(PaymentStateRepository.class);

        when(repository.save(any(PaymentState.class))).thenAnswer(inv -> {
            PaymentState state = inv.getArgument(0);
            stateStore.put(state.paymentReference(), state);
            return state;
        });
        when(repository.findById(anyString())).thenAnswer(inv ->
                Optional.ofNullable(stateStore.get((String) inv.getArgument(0))));

        fixture = AxonTestFixture.with(
                MessagingConfigurer.create()
                                   .componentRegistry(cr -> cr.registerComponent(PaymentStateRepository.class,
                                                                                  cfg -> repository))
                                   .eventProcessing(ep -> ep.subscribing(sp -> sp.defaultProcessor(
                                           "payment-saga",
                                           c -> c.autodetected(cfg -> {
                                               paymentSaga = new PaymentSaga(
                                                       cfg.getComponent(CommandGateway.class),
                                                       cfg.getComponent(PaymentStateRepository.class)
                                               );
                                               return paymentSaga;
                                           })
                                   ))),
                AxonTestFixture.Customization::disableAxonServer
        );
    }

    @Test
    void shouldStartSagaOnBikeRequested() {
        fixture.given()
               .noPriorActivity()
               .when()
               .event(new BikeRequestedEvent("bikeId", "renter", "payRef"))
               .then()
               .commands(new PreparePaymentCommand(10, "payRef"));
    }

    @Test
    void shouldAcceptRequestOnPaymentConfirmed() {
        fixture.given()
               .events(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
               .when()
               .event(new PaymentConfirmedEvent("paymentId", "rentalRef"))
               .then()
               .commands(new ApproveRequestCommand("bikeId", "rentalRef"));
    }

    @Test
    void shouldRejectRequestOnPaymentRejected() {
        fixture.given()
               .events(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
               .when()
               .event(new PaymentRejectedEvent("paymentId", "rentalRef"))
               .then()
               .commands(new RejectRequestCommand("bikeId", "rentalRef"));
    }

    @Test
    void shouldEndSagaWhenRequestIsRejected() {
        fixture.given()
               .events(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
               .when()
               .event(new RequestRejectedEvent("bikeId", "rentalRef"))
               .then()
               .noCommands();
    }

    @Test
    void shouldRejectPaymentWhenNotConfirmedIn30Seconds() {
        var preparedState = new PaymentState("rentalRef", "bikeId", "renter");
        preparedState.prepared("paymentId");
        when(repository.findAllByTimestampLessThanAndStatusIn(anyLong(), any(PaymentState.Status[].class)))
                .thenReturn(List.of(preparedState));

        fixture.given()
               .noPriorActivity()
               .when()
               .nothing()
               .then()
               .expect(config -> paymentSaga.cancelLatePayments())
               .commands(new RejectPaymentCommand("paymentId"));
    }
}
