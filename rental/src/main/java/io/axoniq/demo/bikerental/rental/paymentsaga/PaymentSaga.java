package io.axoniq.demo.bikerental.rental.paymentsaga;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentPreparedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand;
import io.axoniq.demo.bikerental.coreapi.payment.RejectPaymentCommand;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.Scope;
import org.axonframework.messaging.ScopeDescriptor;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

@Saga
public class PaymentSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;

    private String bikeId;
    private String renter;

    @JsonCreator
    public PaymentSaga(String bikeId, String renter) {
        this.bikeId = bikeId;
        this.renter = renter;
    }

    public PaymentSaga() {
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "bikeId")
    public void on(BikeRequestedEvent event) {
        this.bikeId = event.getBikeId();
        this.renter = event.getRenter();
        SagaLifecycle.associateWith("paymentReference", event.getRentalReference());
        preparePayment(event.getRentalReference());
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "paymentReference")
    public void on(PaymentConfirmedEvent event) {
        // we approve the bike request
        commandGateway.send(new ApproveRequestCommand(bikeId, renter));
    }

    @SagaEventHandler(associationProperty = "paymentReference")
    public void on(PaymentRejectedEvent event) {
        commandGateway.send(new RejectRequestCommand(bikeId, renter));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "bikeId")
    public void on(RequestRejectedEvent event) {
        deadlineManager.cancelAllWithinScope("cancelPayment");
    }

    @SagaEventHandler(associationProperty = "paymentReference")
    public void on(PaymentPreparedEvent event) {
        deadlineManager.schedule(Duration.ofSeconds(30), "cancelPayment", event.getPaymentId());
    }

    @DeadlineHandler(deadlineName = "cancelPayment")
    public void cancelPayment(String paymentId) {
        commandGateway.send(new RejectPaymentCommand(paymentId));
    }

    @DeadlineHandler(deadlineName = "retryPayment")
    public void preparePayment(String rentalReference) {
        ScopeDescriptor scope = Scope.describeCurrentScope();
        commandGateway.send(new PreparePaymentCommand(10, rentalReference))
                      .whenComplete((r, e) -> {
                          if (e != null) {
                              deadlineManager.schedule(Duration.ofSeconds(5), "retryPayment", rentalReference, scope);
                          }
                      });
    }

    public String getBikeId() {
        return bikeId;
    }

    public String getRenter() {
        return renter;
    }

}
