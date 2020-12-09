package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.ConfirmPaymentCommand;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class PaymentController {

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;

    public PaymentController(QueryGateway queryGateway, CommandGateway commandGateway) {
        this.queryGateway = queryGateway;
        this.commandGateway = commandGateway;
    }

    @GetMapping("/{paymentId}")
    public CompletableFuture<PaymentStatus> getStatus(@PathVariable("paymentId")String paymentId) {
        return queryGateway.query("getStatus", paymentId, PaymentStatus.class);
    }

    @GetMapping("/{paymentId}/confirm")
    public CompletableFuture<Void> confirmPayment(@PathVariable("paymentId")String paymentId) {
        return commandGateway.send(new ConfirmPaymentCommand(paymentId));
    }


    @GetMapping("/")
    public CompletableFuture<List<PaymentStatus>> getStatus(@RequestParam(value = "status", required = false) PaymentStatus.Status status) {
        return queryGateway.query("getAllPayments", status, ResponseTypes.multipleInstancesOf(PaymentStatus.class));
    }

//    @GetMapping("/preparePayment")
//    public CompletableFuture<String> createSome() {
//        return commandGateway.send(new PreparePaymentCommand(ThreadLocalRandom.current().nextInt(10, 100)));
//    }
}
