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
        repository.save(new PaymentState(event.rentalReference(), event.renter()));
        commandDispatcher.send(new PreparePaymentCommand(10, event.rentalReference()));
    }

    @EventHandler
    public void on(PaymentConfirmedEvent event, CommandDispatcher commandDispatcher) {
        repository.updateStatus(event.paymentReference(), PaymentState.Status.CONFIRMED);
        commandDispatcher.send(new ApproveRequestCommand(event.paymentReference()));
    }

    @EventHandler
    public void on(PaymentRejectedEvent event, CommandDispatcher commandDispatcher) {
        repository.updateStatus(event.paymentReference(), PaymentState.Status.REJECTED);
        commandDispatcher.send(new RejectRequestCommand(event.paymentReference()));
    }

    @Scheduled(fixedDelay = 1000)
    public void cancelLatePayments() {
        long cutoffTime = System.currentTimeMillis() - Duration.ofSeconds(10).toMillis();
        repository.findAllByTimestampLessThanAndStatusIn(cutoffTime, PaymentState.Status.PREPARED, PaymentState.Status.PENDING)
                  .forEach(state -> {
                      if (state.paymentId() != null) {
                          commandGateway.send(new RejectPaymentCommand(state.paymentId()));
                      } else {
                          repository.updateStatus(state.paymentReference(), PaymentState.Status.CANCELLED);
                          commandGateway.send(new RejectRequestCommand(state.paymentReference()));
                      }
                  });
    }

    @EventHandler
    public void on(PaymentPreparedEvent event) {
        repository.updateStatusAndPaymentId(event.paymentReference(), event.paymentId(), PaymentState.Status.PREPARED);
    }
}
