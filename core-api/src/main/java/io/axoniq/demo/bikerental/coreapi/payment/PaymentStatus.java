package io.axoniq.demo.bikerental.coreapi.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.axonframework.messaging.queryhandling.annotation.QueryResponse;

@Entity
@QueryResponse
public class PaymentStatus {
    @Id
    private String id;

    private Status status;
    private int amount;
    private String reference;

    public PaymentStatus() {
    }

    public PaymentStatus(String id, int amount, String reference, Status status) {
        this.id = id;
        this.amount = amount;
        this.reference = reference;
        this.status = status;
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

    public String getReference() {
        return reference;
    }

    public enum Status {

        PENDING, APPROVED, REJECTED
    }

    public PaymentStatus withStatus(Status status) {
        return new PaymentStatus(id, amount, reference, status);
    }
}
