package io.axoniq.demo.bikerental.rental.paymentsaga;

import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.Scope;
import org.axonframework.messaging.ScopeDescriptor;
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

    @StartSaga
    @SagaEventHandler(associationProperty = "bikeId")
    public void on(BikeRequestedEvent event) {
        this.bikeId = event.getBikeId();
        this.renter = event.getRenter();
        SagaLifecycle.associateWith("paymentReference", event.getRentalReference());
        preparePayment(event.getRentalReference());
    }

    @SagaEventHandler(associationProperty = "paymentReference")
    public void on(PaymentConfirmedEvent event) {
        // we approve the bike request
        commandGateway.send(new ApproveRequestCommand(bikeId, renter));
    }

    @SagaEventHandler(associationProperty = "paymentReference")
    public void on(PaymentRejectedEvent event) {
        commandGateway.send(new RejectRequestCommand(bikeId, renter));
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

}
