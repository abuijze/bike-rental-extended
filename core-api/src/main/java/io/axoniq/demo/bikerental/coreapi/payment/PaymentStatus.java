package io.axoniq.demo.bikerental.coreapi.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PaymentStatus {
    @Id
    private String id;

    private Status status;
    private int amount;
    private String reference;

    public PaymentStatus() {
    }

    public PaymentStatus(String id, int amount, String reference) {
        this.id = id;
        this.amount = amount;
        this.reference = reference;
        this.status = Status.PENDING;
    }

    public String getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public int getAmount() {
        return amount;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {

        PENDING, APPROVED, REJECTED
    }
}
