package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.ConfirmPaymentCommand;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentPreparedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand;
import io.axoniq.demo.bikerental.coreapi.payment.RejectPaymentCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class Payment {

    @AggregateIdentifier
    private String id;

    private boolean closed;
    private String paymentReference;

    public Payment() {
    }

    @CommandHandler
    public Payment(PreparePaymentCommand command) {
        String paymentId = UUID.randomUUID().toString();
        apply(new PaymentPreparedEvent(paymentId, command.getAmount(), command.getPaymentReference()));
    }

    @CommandHandler
    public void handle(ConfirmPaymentCommand command) {
        if (!closed) {
            apply(new PaymentConfirmedEvent(command.getPaymentId(), paymentReference));
        }
    }

    @CommandHandler
    public void handle(RejectPaymentCommand command) {
        if (!closed) {
            apply(new PaymentRejectedEvent(command.getPaymentId(), paymentReference));
        }
    }

    @EventSourcingHandler
    protected void on(PaymentPreparedEvent event) {
        this.id = event.getPaymentId();
        this.paymentReference = event.getPaymentReference();
    }

    @EventSourcingHandler
    protected void on(PaymentConfirmedEvent event) {
        this.closed = true;
    }

    @EventSourcingHandler
    protected void on(PaymentRejectedEvent event) {
        this.closed = true;
    }
}
