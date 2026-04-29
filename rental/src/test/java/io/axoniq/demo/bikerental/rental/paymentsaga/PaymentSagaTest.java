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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentSagaTest {

    private PaymentStateRepository repository;
    private ScheduledExecutorService mockScheduler;
    private PaymentSaga paymentSaga;
    private AxonTestFixture fixture;
    private Map<String, PaymentState> stateStore;

    @AfterEach
    void tearDown() {
        fixture.stop();
    }

    @BeforeEach
    void setUp() {
        stateStore = new HashMap<>();
        repository = mock(PaymentStateRepository.class);
        mockScheduler = mock(ScheduledExecutorService.class);

        when(repository.save(any(PaymentState.class))).thenAnswer(inv -> {
            PaymentState state = inv.getArgument(0);
            stateStore.put(state.paymentReference(), state);
            return state;
        });
        when(repository.findById(anyString())).thenAnswer(inv ->
                Optional.ofNullable(stateStore.get((String) inv.getArgument(0))));
        when(repository.findEarliestActiveTimestamp(any())).thenReturn(Optional.empty());

        fixture = AxonTestFixture.with(
                MessagingConfigurer.create()
                                   .componentRegistry(cr -> cr.registerComponent(PaymentStateRepository.class,
                                                                                  cfg -> repository))
                                   .eventProcessing(ep -> ep.subscribing(sp -> sp.defaultProcessor(
                                           "payment-saga",
                                           c -> c.autodetected("paymentSaga", cfg -> {
                                               paymentSaga = new PaymentSaga(
                                                       cfg.getComponent(CommandGateway.class),
                                                       cfg.getComponent(PaymentStateRepository.class),
                                                       mockScheduler
                                               );
                                               return paymentSaga;
                                           })
                                   ))));
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
               .commands(new ApproveRequestCommand("rentalRef"));
    }

    @Test
    void shouldRejectRequestOnPaymentRejected() {
        fixture.given()
               .events(new BikeRequestedEvent("bikeId", "renter", "rentalRef"))
               .when()
               .event(new PaymentRejectedEvent("paymentId", "rentalRef"))
               .then()
               .commands(new RejectRequestCommand("rentalRef"));
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
        var preparedState = new PaymentState("rentalRef");
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

    @Nested
    class SchedulingBehavior {

        private ScheduledFuture<?> mockFuture;

        @BeforeEach
        void setUp() {
            mockFuture = mock(ScheduledFuture.class);
            doReturn(mockFuture).when(mockScheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        }

        @Test
        void schedulesCheckOnFirstPaymentArrival() {
            when(repository.findEarliestActiveTimestamp(any())).thenReturn(Optional.of(System.currentTimeMillis()));

            fixture.given().noPriorActivity()
                   .when().event(new BikeRequestedEvent("bikeId", "renter", "payRef"))
                   .then().success();

            verify(mockScheduler, times(1)).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        }

        @Test
        void doesNotScheduleDuplicateCheckWhenOneIsActive() {
            when(mockFuture.isDone()).thenReturn(false);
            when(repository.findEarliestActiveTimestamp(any())).thenReturn(Optional.of(System.currentTimeMillis()));

            fixture.given()
                   .events(new BikeRequestedEvent("bikeId1", "renter1", "payRef1"))
                   .when().event(new BikeRequestedEvent("bikeId2", "renter2", "payRef2"))
                   .then().success();

            verify(mockScheduler, times(1)).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        }

        @Test
        void schedulesNextCheckAfterCancellation() {
            when(repository.findAllByTimestampLessThanAndStatusIn(anyLong(), any(PaymentState.Status[].class)))
                    .thenReturn(List.of());
            when(repository.findEarliestActiveTimestamp(any())).thenReturn(Optional.of(System.currentTimeMillis() + 5000));

            fixture.given().noPriorActivity()
                   .when().nothing()
                   .then().expect(config -> paymentSaga.cancelLatePayments());

            verify(mockScheduler, times(1)).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        }

        @Test
        void doesNotRescheduleWhenNoPaymentsPendingAfterCancellation() {
            when(repository.findAllByTimestampLessThanAndStatusIn(anyLong(), any(PaymentState.Status[].class)))
                    .thenReturn(List.of());

            fixture.given().noPriorActivity()
                   .when().nothing()
                   .then().expect(config -> paymentSaga.cancelLatePayments());

            verify(mockScheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        }
    }
}
