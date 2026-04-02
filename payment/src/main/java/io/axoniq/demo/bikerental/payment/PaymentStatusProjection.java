package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.*;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.queryhandling.QueryUpdateEmitter;
import org.axonframework.messaging.queryhandling.annotation.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus.Status.*;

@Component
public class PaymentStatusProjection {

    private final PaymentStatusRepository paymentStatusRepository;

    public PaymentStatusProjection(PaymentStatusRepository paymentStatusRepository) {
        this.paymentStatusRepository = paymentStatusRepository;
    }

    @QueryHandler
    public PaymentStatus getStatus(GetStatusQuery query) {
        return paymentStatusRepository.findById(query.paymentId()).orElse(null);
    }

    @QueryHandler
    public String getPaymentId(GetPaymentIdQuery query) {
        return paymentStatusRepository.findByReferenceAndStatus(query.paymentReference(), PENDING).map(PaymentStatus::getId).orElse(null);
    }

    @QueryHandler
    public Iterable<PaymentStatus> findByStatus(GetAllPaymentsQuery query) {
        return paymentStatusRepository.findAllByStatus(query.paymentStatus());
    }

    @EventHandler
    public void handle(PaymentPreparedEvent event, QueryUpdateEmitter updateEmitter) {
        paymentStatusRepository.save(new PaymentStatus(event.paymentId(), event.amount(), event.paymentReference(), PENDING));
        updateEmitter.emit(GetPaymentIdQuery.class,
                           q -> Objects.equals(event.paymentReference(), q.paymentReference()),
                           event.paymentId());
    }

    @EventHandler
    public void handle(PaymentConfirmedEvent event) {
        paymentStatusRepository.findById(event.paymentId()).ifPresent(s -> s.withStatus(APPROVED));
    }

    @EventHandler
    public void handle(PaymentRejectedEvent event) {
        paymentStatusRepository.findById(event.paymentId()).ifPresent(s -> s.withStatus(REJECTED));
    }
}
