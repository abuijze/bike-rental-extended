package io.axoniq.demo.bikerental.rental.paymentsaga;

import io.axoniq.demo.bikerental.coreapi.payment.*;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import org.axonframework.messaging.commandhandling.gateway.CommandDispatcher;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.eventhandling.replay.annotation.DisallowReplay;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@DisallowReplay
public class PaymentSaga {

    private final CommandGateway commandGateway;
    private final PaymentStateRepository repository;

    public PaymentSaga(CommandGateway commandGateway,
                       PaymentStateRepository repository) {
        this.commandGateway = commandGateway;
        this.repository = repository;
    }

    @EventHandler
    public void on(BikeRequestedEvent event, CommandDispatcher commandDispatcher) {
        repository.save(new PaymentState(event.rentalReference(), event.bikeId(), event.renter()));
        commandDispatcher.send(new PreparePaymentCommand(10, event.rentalReference()));
    }

    @EventHandler
    public void on(PaymentConfirmedEvent event, CommandDispatcher commandDispatcher) {
        // we approve the bike request
        repository.findById(event.paymentReference())
                  .ifPresent(status -> {
                      status.setStatus(PaymentState.Status.CONFIRMED);
                      commandDispatcher.send(new ApproveRequestCommand(status.bikeId(), status.paymentReference()));
                  });
    }

    @EventHandler
    public void on(PaymentRejectedEvent event, CommandDispatcher commandDispatcher) {
        repository.findById(event.paymentReference())
                  .ifPresent(state -> {
                      state.setStatus(PaymentState.Status.REJECTED);
                      commandDispatcher.send(new RejectRequestCommand(state.bikeId(), state.paymentReference()));
                  });
    }

    @Scheduled(fixedDelay = 1000)
    public void cancelLatePayments() {
        long cutoffTime = System.currentTimeMillis() - Duration.ofSeconds(10).toMillis();
        repository.findAllByTimestampLessThanAndStatusIn(cutoffTime, PaymentState.Status.PREPARED, PaymentState.Status.PENDING)
                  .forEach(state -> {if(state.paymentId() != null) commandGateway.send(new RejectPaymentCommand(state.paymentId())); });
    }

    @EventHandler
    public void on(PaymentPreparedEvent event) {
        repository.findById(event.paymentReference())
                  .ifPresent(state -> {
                      state.prepared(event.paymentId());
                  });
    }
}
