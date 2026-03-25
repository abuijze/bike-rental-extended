package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.*;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.extension.spring.stereotype.EventSourced;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;

@EventSourced
public class Payment {

    private boolean closed;
    private String paymentReference;

    @EntityCreator
    public Payment(PaymentPreparedEvent event) {
        this.paymentReference = event.getPaymentReference();
    }

    @CommandHandler
    public void handle(ConfirmPaymentCommand command, EventAppender appender) {
        if (!closed) {
            appender.append(new PaymentConfirmedEvent(command.getPaymentId(), paymentReference));
        }
    }

    @CommandHandler
    public void handle(RejectPaymentCommand command, EventAppender appender) {
        if (!closed) {
            appender.append(new PaymentRejectedEvent(command.getPaymentId(), paymentReference));
        }
    }

    @EventSourcingHandler
    protected void on(PaymentPreparedEvent event) {
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
