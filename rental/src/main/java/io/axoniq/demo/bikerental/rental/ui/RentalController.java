package io.axoniq.demo.bikerental.rental.ui;

import io.axoniq.demo.bikerental.coreapi.payment.ConfirmPaymentCommand;
import io.axoniq.demo.bikerental.coreapi.payment.GetPaymentIdQuery;
import io.axoniq.demo.bikerental.coreapi.payment.RejectPaymentCommand;
import io.axoniq.demo.bikerental.coreapi.rental.*;
import org.axonframework.common.lifecycle.ShutdownInProgressException;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.queryhandling.gateway.QueryGateway;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/")
public class RentalController {

    public static final FindAll FIND_ALL_QUERY = new FindAll();
    private static final List<String> RENTERS = Arrays.asList("Allard", "Steven", "Josh", "David", "Marc", "Sara", "Milan", "Jeroen", "Marina", "Jeannot");
    private static final List<String> LOCATIONS = Arrays.asList("Amsterdam", "Paris", "Vilnius", "Barcelona", "London", "New York", "Toronto", "Berlin", "Milan", "Rome", "Belgrade");
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final ShutdownListener shutdownListener;

    public RentalController(CommandGateway commandGateway, QueryGateway queryGateway, ShutdownListener shutdownListener) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.shutdownListener = shutdownListener;
    }

    @PostMapping("/bikes")
    public CompletableFuture<Void> generateBikes(@RequestParam("count") int bikeCount,
                                                 @RequestParam(value = "type") String bikeType) {
        CompletableFuture<Void> all = CompletableFuture.completedFuture(null);
        for (int i = 0; i < bikeCount; i++) {
            all = CompletableFuture.allOf(all,
                                          commandGateway.send(new RegisterBikeCommand(UUID.randomUUID().toString(), bikeType, randomLocation()))
                                                        .getResultMessage());
        }
        return all;
    }

    // Lazy solution to not have the UI point at different URLS
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


    @GetMapping("/bikes")
    public CompletableFuture<List<BikeStatus>> findAll() {
        return queryGateway.queryMany(FIND_ALL_QUERY, BikeStatus.class);
    }

    @GetMapping("/bikeUpdates")
    public Flux<ServerSentEvent<String>> subscribeToAllUpdates() {
        return Flux.from(queryGateway.subscriptionQuery(FIND_ALL_QUERY, BikeStatus.class))
                   .map(BikeStatus::description)
                   .map(description -> ServerSentEvent.builder(description).build());
    }

    /*
Event source spec does not support headers, so we go for another url
See https://html.spec.whatwg.org/multipage/server-sent-events.html#the-eventsource-interface
 */
    @GetMapping("/bikeUpdatesJson")
    public Flux<ServerSentEvent<BikeStatus>> subscribeToAllUpdatesJson() {
        return shutdownListener
                .closedOnShutdown(queryGateway.subscriptionQuery(FIND_ALL_QUERY, BikeStatus.class))
                .map(description -> ServerSentEvent.builder(description).build())
                .onErrorComplete(ShutdownInProgressException.class);

    }

    @GetMapping("/bikeUpdates/{bikeId}")
    public Flux<ServerSentEvent<String>> subscribeToBikeUpdates(@PathVariable("bikeId") String bikeId) {
        return Flux.from(queryGateway.subscriptionQuery(
                           new FindOne(bikeId),
                           BikeStatus.class))
                   .map(BikeStatus::description)
                   .map(description -> ServerSentEvent.builder(description).build());
    }

    @PostMapping("/requestBike")
    public CompletableFuture<String> requestBike(@RequestParam("bikeId") String bikeId,
                                                 @RequestParam(value = "renter", required = false) String renter) {
        return commandGateway.send(new RequestBikeCommand(bikeId, renter != null ? renter : randomRenter()))
                             .resultAs(String.class);

    }

    @PostMapping("/returnBike")
    public CompletableFuture<String> returnBike(@RequestParam("bikeId") String bikeId,
                                                @RequestParam("rentalReference") String rentalReference) {
        return commandGateway.send(new ReturnBikeCommand(bikeId, rentalReference, randomLocation()))
                             .resultAs(String.class);
    }

    @PostMapping("/revokeRequest")
    public CompletableFuture<Void> revokeRequest(@RequestParam("rentalReference") String rentalReference) {
        return commandGateway.send(new RejectRequestCommand(rentalReference), Void.class);
    }

    @GetMapping(value = "watch", produces = "text/event-stream")
    public Flux<String> watchAll() {
        return Flux.from(queryGateway.subscriptionQuery(
                FIND_ALL_QUERY,
                BikeStatus.class))
                   .map(bs -> bs.getBikeId() + " -> " + bs.description());
    }

    @GetMapping(value = "watch/{bikeId}", produces = "text/event-stream")
    public Flux<String> watchBike(@PathVariable("bikeId") String bikeId) {
        return Flux.from(queryGateway.subscriptionQuery(
                           new FindOne(bikeId),
                           BikeStatus.class))
                   .map(bs -> bs.getBikeId() + " -> " + bs.description());
    }

    @PostMapping(value = "/generateRentals")
    public Flux<String> generateData(@RequestParam(value = "bikeType") String bikeType,
                                     @RequestParam("loops") int loops,
                                     @RequestParam(value = "concurrency", defaultValue = "1") int concurrency,
                                     @RequestParam(value = "abandonPaymentFactor", defaultValue = "100") int abandonPaymentFactor,
                                     @RequestParam(value = "delay", defaultValue = "0") int delay) {

        return Flux.range(0, loops)
                   .flatMap(j -> executeRentalCycle(bikeType, randomRenter(), abandonPaymentFactor, delay)
                                    .map(r -> "OK - Rented, Payed and Returned\n")
                                    .onErrorResume(e -> Mono.just("Not ok: " + e.getMessage() + "\n")),
                            concurrency);
    }

    @GetMapping("/bikes/{bikeId}")
    public CompletableFuture<BikeStatus> findStatus(@PathVariable("bikeId") String bikeId) {
        return queryGateway.query(new FindOne(bikeId), BikeStatus.class);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)   //(1)
    public Object exceptionHandler(IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {   //(2)
            return null;        //(2)	socket is closed, cannot return any response
        } else {
            return new HttpEntity<>(e.getMessage());  //(3)
        }
    }

    private Mono<String> executeRentalCycle(String bikeType, String renter, int abandonPaymentFactor, int delay) {
        CompletableFuture<String> result = selectRandomAvailableBike(bikeType)
                .thenCompose(bikeId -> commandGateway.send(new RequestBikeCommand(bikeId, renter))
                                                     .resultAs(String.class)
                                                     .thenComposeAsync(rentalRef -> executePayment(bikeId,
                                                                                                   rentalRef,
                                                                                                   abandonPaymentFactor)
                                                                               .thenCompose(r -> whenBikeUnlocked(bikeId))
                                                                               .thenComposeAsync(r -> commandGateway.send(new ReturnBikeCommand(
                                                                                                         bikeId,
                                                                                                         rentalRef,
                                                                                                         randomLocation())).getResultMessage(),
                                                                                                 CompletableFuture.delayedExecutor(randomDelay(delay), TimeUnit.MILLISECONDS))
                                                                               .thenApply(r -> bikeId),
                                                                       CompletableFuture.delayedExecutor(randomDelay(
                                                                               delay), TimeUnit.MILLISECONDS)));
        return Mono.fromFuture(result);
    }

    private int randomDelay(int delay) {
        if (delay <= 0) {
            return 0;
        }
        return ThreadLocalRandom.current().nextInt(delay - (delay >> 2), delay + delay + (delay >> 2));
    }

    private CompletableFuture<String> selectRandomAvailableBike(String bikeType) {
        return queryGateway.queryMany(new FindAvailable(bikeType), BikeStatus.class)
                           .thenApply(this::pickRandom)
                           .thenApply(BikeStatus::getBikeId);
    }

    private <T> T pickRandom(List<T> source) {
        return source.get(ThreadLocalRandom.current().nextInt(source.size()));
    }

    private CompletableFuture<String> whenBikeUnlocked(String bikeId) {
        return Flux.from(queryGateway.subscriptionQuery(new FindOne(bikeId),
                                                        BikeStatus.class))
                   .any(status -> status.getStatus() == RentalStatus.RENTED)
                          .map(s -> bikeId)
                          .toFuture();
    }

    private CompletableFuture<String> executePayment(String bikeId, String paymentRef, int abandonPaymentFactor) {
        if (abandonPaymentFactor > 0 && ThreadLocalRandom.current().nextInt(abandonPaymentFactor) == 0) {
            return CompletableFuture.failedFuture(new IllegalStateException("Customer refused to pay"));
        }
        return Flux.from(queryGateway.subscriptionQuery(new GetPaymentIdQuery(paymentRef),
                                                        String.class))
                   .filter(Objects::nonNull)
                          .next()
                   .flatMap(paymentId -> Mono.fromFuture(commandGateway.send(new ConfirmPaymentCommand(paymentId)).getResultMessage()))
                          .map(o -> bikeId)
                   .timeout(Duration.ofSeconds(5))
                   .toFuture();
    }

    private String randomRenter() {
        return RENTERS.get(ThreadLocalRandom.current().nextInt(RENTERS.size()));
    }

    private String randomLocation() {
        return LOCATIONS.get(ThreadLocalRandom.current().nextInt(LOCATIONS.size()));
    }
}
