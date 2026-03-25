package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.*;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.queryhandling.gateway.QueryGateway;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class PaymentController {

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;

    public PaymentController(QueryGateway queryGateway, CommandGateway commandGateway) {
        this.queryGateway = queryGateway;
        this.commandGateway = commandGateway;
    }

    @GetMapping("/status/{paymentId}")
    public CompletableFuture<PaymentStatus> getStatus(@PathVariable("paymentId") String paymentId) {
        return queryGateway.query(new GetStatusQuery(paymentId), PaymentStatus.class);
    }

    @GetMapping("/findPayment")
    public CompletableFuture<String> findPaymentId(@RequestParam("reference") String paymentReference) {
        return queryGateway.query(new GetPaymentIdQuery(paymentReference), String.class);
    }

    @PostMapping("/acceptPayment")
    public CompletableFuture<Void> confirmPayment(@RequestParam("id") String paymentId) {
        return commandGateway.send(new ConfirmPaymentCommand(paymentId), Void.class);
    }

    @PostMapping("/rejectPayment")
    public CompletableFuture<Void> rejectPayment(@RequestParam("id") String paymentId) {
        return commandGateway.send(new RejectPaymentCommand(paymentId), Void.class);
    }

    @GetMapping("/status")
    public Flux<PaymentStatus> getStatus(@RequestParam(value = "status", required = false) PaymentStatus.Status status) {
        return Flux.from(queryGateway.streamingQuery(new GetAllPaymentsQuery(status), PaymentStatus.class));
    }

}
