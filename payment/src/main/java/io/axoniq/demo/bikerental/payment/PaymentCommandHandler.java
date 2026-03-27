package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.PaymentPreparedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentCommandHandler {

    @CommandHandler
    public static String handle(PreparePaymentCommand command, EventAppender appender) {
        String paymentId = UUID.randomUUID().toString();
        appender.append(new PaymentPreparedEvent(paymentId, command.amount(), command.paymentReference()));
        return paymentId;
    }
}
