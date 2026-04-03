package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.*;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.extension.spring.stereotype.EventSourced;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.modelling.annotation.InjectEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentCommandHandler {

    @CommandHandler
    static String handle(PreparePaymentCommand command, EventAppender appender) {
        String paymentId = UUID.randomUUID().toString();
        appender.append(new PaymentPreparedEvent(paymentId, command.amount(), command.paymentReference()));
        return paymentId;
    }

    @CommandHandler
    void handle(ConfirmPaymentCommand command, @InjectEntity Payment payment, EventAppender appender) {
        if (!payment.isClosed()) {
            appender.append(new PaymentConfirmedEvent(command.paymentId(), payment.getPaymentReference()));
        }
    }

    @CommandHandler
    void handle(RejectPaymentCommand command, @InjectEntity Payment payment, EventAppender appender) {
        if (!payment.isClosed()) {
            appender.append(new PaymentRejectedEvent(command.paymentId(), payment.getPaymentReference()));
        }
    }

    @EventSourced(tagKey = "Payment")
    static class Payment {

        private boolean closed;
        private final String paymentReference;

        @EntityCreator
        public Payment(PaymentPreparedEvent event) {
            this.paymentReference = event.paymentReference();
        }

        @EventSourcingHandler
        protected void on(PaymentConfirmedEvent event) {
            this.closed = true;
        }

        @EventSourcingHandler
        protected void on(PaymentRejectedEvent event) {
            this.closed = true;
        }

        public boolean isClosed() {
            return closed;
        }

        public String getPaymentReference() {
            return paymentReference;
        }
    }
}
