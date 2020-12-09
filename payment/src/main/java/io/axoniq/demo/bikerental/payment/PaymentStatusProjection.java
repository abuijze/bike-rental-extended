package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentPreparedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

import static io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus.Status.APPROVED;
import static io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus.Status.PENDING;
import static io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus.Status.REJECTED;

@Component
public class PaymentStatusProjection {

    private final PaymentStatusRepository paymentStatusRepository;
    private final QueryUpdateEmitter updateEmitter;

    public PaymentStatusProjection(PaymentStatusRepository paymentStatusRepository,
                                   QueryUpdateEmitter updateEmitter) {
        this.paymentStatusRepository = paymentStatusRepository;
        this.updateEmitter = updateEmitter;
    }

    @QueryHandler(queryName = "getStatus")
    public PaymentStatus getStatus(String paymentId) {
        return paymentStatusRepository.findById(paymentId).orElse(null);
    }

    @QueryHandler(queryName = "getPaymentId")
    public String getPaymentId(String paymentReference) {
        return paymentStatusRepository.findByReferenceAndStatus(paymentReference, PENDING).map(PaymentStatus::getId).orElse(null);
    }

    @QueryHandler(queryName = "getAllPayments")
    public Iterable<PaymentStatus> findByStatus(PaymentStatus.Status status) {
        if (status == null) {
            return paymentStatusRepository.findAll();
        }
        return paymentStatusRepository.findAllByStatus(status);
    }

    @QueryHandler(queryName = "getAllPayments")
    public Iterable<PaymentStatus> findAll() {
        return paymentStatusRepository.findAll();
    }

    @EventHandler
    public void handle(PaymentPreparedEvent event) {
        paymentStatusRepository.save(new PaymentStatus(event.getPaymentId(), event.getAmount(), event.getPaymentReference()));
        updateEmitter.emit(String.class, event.getPaymentReference()::equals, event.getPaymentId());
    }

    @EventHandler
    public void handle(PaymentConfirmedEvent event) {
        paymentStatusRepository.findById(event.getPaymentId()).ifPresent(s -> s.setStatus(APPROVED));
    }

    @EventHandler
    public void handle(PaymentRejectedEvent event) {
        paymentStatusRepository.findById(event.getPaymentId()).ifPresent(s -> s.setStatus(REJECTED));
    }
}
