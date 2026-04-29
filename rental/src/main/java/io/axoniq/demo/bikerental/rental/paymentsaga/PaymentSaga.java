package io.axoniq.demo.bikerental.rental.paymentsaga;

import io.axoniq.demo.bikerental.coreapi.payment.*;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import org.axonframework.messaging.commandhandling.gateway.CommandDispatcher;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.eventhandling.replay.annotation.DisallowReplay;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
@DisallowReplay
public class PaymentSaga {

    private static final Duration PAYMENT_TIMEOUT = Duration.ofSeconds(10);

    private final CommandGateway commandGateway;
    private final PaymentStateRepository repository;
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<ScheduledFuture<?>> scheduledCheck = new AtomicReference<>();

    public PaymentSaga(CommandGateway commandGateway,
                       PaymentStateRepository repository,
                       ScheduledExecutorService workerExecutorService) {
        this.commandGateway = commandGateway;
        this.repository = repository;
        this.scheduler = workerExecutorService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        scheduleNextCheck();
    }

    @EventHandler
    public void on(BikeRequestedEvent event, CommandDispatcher commandDispatcher) {
        repository.save(new PaymentState(event.rentalReference()));
        commandDispatcher.send(new PreparePaymentCommand(10, event.rentalReference()));
        ScheduledFuture<?> current = scheduledCheck.get();
        if (current == null || current.isDone()) {
            scheduleNextCheck();
        }
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

    @EventHandler
    public void on(PaymentPreparedEvent event) {
        repository.updateStatusAndPaymentId(event.paymentReference(), event.paymentId(), PaymentState.Status.PREPARED);
    }

    @Transactional
    public void cancelLatePayments() {
        long cutoffTime = System.currentTimeMillis() - PAYMENT_TIMEOUT.toMillis();
        repository.findAllByTimestampLessThanAndStatusIn(cutoffTime, PaymentState.Status.PREPARED, PaymentState.Status.PENDING)
                  .forEach(state -> {
                      if (state.paymentId() != null) {
                          commandGateway.send(new RejectPaymentCommand(state.paymentId()));
                      } else {
                          repository.updateStatus(state.paymentReference(), PaymentState.Status.CANCELLED);
                          commandGateway.send(new RejectRequestCommand(state.paymentReference()));
                      }
                  });
        scheduleNextCheck();
    }

    private void scheduleNextCheck() {
        repository.findEarliestActiveTimestamp(List.of(PaymentState.Status.PENDING, PaymentState.Status.PREPARED))
                  .ifPresent(earliestTimestamp -> {
                      long delay = earliestTimestamp + PAYMENT_TIMEOUT.toMillis() - System.currentTimeMillis();
                      scheduledCheck.set(scheduler.schedule(this::cancelLatePayments, Math.max(delay, 0), TimeUnit.MILLISECONDS));
                  });
    }
}
