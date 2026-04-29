package io.axoniq.demo.bikerental.rental.paymentsaga;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PaymentState {
    @Id
    private String paymentReference;
    private Status status;
    private long timestamp;
    private String paymentId;

    public PaymentState() {
    }

    public PaymentState(String paymentReference) {
        this.paymentReference = paymentReference;
        this.status = Status.PENDING;
        this.timestamp = System.currentTimeMillis();
    }

    public String paymentReference() {
        return paymentReference;
    }

    public Status status() {
        return status;
    }

    public void prepared(String paymentId) {
        this.status = Status.PREPARED;
        this.paymentId = paymentId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String paymentId() {
        return paymentId;
    }

    public enum Status {
        PENDING,
        PREPARED,
        CONFIRMED,
        REJECTED,
        CANCELLED
    }
}
