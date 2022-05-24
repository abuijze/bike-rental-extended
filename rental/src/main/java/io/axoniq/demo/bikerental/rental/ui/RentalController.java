package io.axoniq.demo.bikerental.rental.ui;

import io.axoniq.demo.bikerental.coreapi.payment.ConfirmPaymentCommand;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import io.axoniq.demo.bikerental.coreapi.rental.RegisterBikeCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RentalStatus;
import io.axoniq.demo.bikerental.coreapi.rental.RequestBikeCommand;
import io.axoniq.demo.bikerental.coreapi.rental.ReturnBikeCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/")
public class RentalController {

    private static final List<String> RENTERS = Arrays.asList("Allard", "Steven", "Josh", "David", "Marc", "Sara", "Milan", "Jeroen", "Marina", "Jeannot");
    private static final List<String> LOCATIONS = Arrays.asList("Sagrada Família", "Barri Gòtic", "Casa Milà", "La Rambla", "Playa de Bogatell", "Palau de la Música Catalana", "Catedral de la Santa Cruz y Santa Eulalia", "Parc Güell", "Palau de Congressos", "Font Màgica de Montjuïc");
    public static final String FIND_ALL_QUERY = "findAll";
    public static final String FIND_AVAILABLE = "findAvailable";
    public static final String FIND_ONE_QUERY = "findOne";
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public RentalController(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping
    public CompletableFuture<Void> generateBikes(@RequestParam("bikes") int bikeCount,
                                                 @RequestParam(value = "bikeType") String bikeType) {
        CompletableFuture<Void> all = CompletableFuture.completedFuture(null);
        for (int i = 0; i < bikeCount; i++) {
            all = CompletableFuture.allOf(all,
                                          commandGateway.send(new RegisterBikeCommand(UUID.randomUUID().toString(), bikeType, randomLocation())));
        }
        return all;
    }

    @GetMapping("/bikes")
    public CompletableFuture<List<BikeStatus>> findAll() {
        return queryGateway.query(FIND_ALL_QUERY, null, ResponseTypes.multipleInstancesOf(BikeStatus.class));
    }

    @GetMapping("/availableBikes")
    public CompletableFuture<List<BikeStatus>> findAvailable(@RequestParam(value = "bikeType", defaultValue = "city") String bikeType) {
        return queryGateway.query(FIND_AVAILABLE, bikeType, ResponseTypes.multipleInstancesOf(BikeStatus.class));
    }

    @GetMapping("/availableBikeLocations")
    public Mono<List<String>> findAvailableLocations(@RequestParam(value = "bikeType", defaultValue = "city") String bikeType) {
        return Mono.fromFuture(() -> queryGateway.query(FIND_AVAILABLE, bikeType, ResponseTypes.multipleInstancesOf(BikeStatus.class)))
                   .flatMapMany(Flux::fromIterable)
                   .map(BikeStatus::getLocation)
                   .distinct()
                   .collectList();
    }

    @GetMapping("/bikeUpdates")
    public Flux<ServerSentEvent<String>> subscribeToAllUpdates() {
        SubscriptionQueryResult<List<BikeStatus>, BikeStatus> subscriptionQueryResult = queryGateway.subscriptionQuery(FIND_ALL_QUERY, null, ResponseTypes.multipleInstancesOf(BikeStatus.class), ResponseTypes.instanceOf(BikeStatus.class));
        return subscriptionQueryResult.initialResult()
                                      .flatMapMany(Flux::fromIterable)
                                      .concatWith(subscriptionQueryResult.updates())
                                      .doFinally(s -> subscriptionQueryResult.close())
                                      .map(BikeStatus::description)
                                      .map(description -> ServerSentEvent.builder(description).build());
    }

    @GetMapping("/bikeUpdates/{bikeId}")
    public Flux<ServerSentEvent<String>> subscribeToBikeUpdates(@PathVariable("bikeId") String bikeId) {
        SubscriptionQueryResult<BikeStatus, BikeStatus> subscriptionQueryResult = queryGateway.subscriptionQuery(FIND_ONE_QUERY, bikeId, BikeStatus.class, BikeStatus.class);
        return subscriptionQueryResult.initialResult()
                                      .concatWith(subscriptionQueryResult.updates())
                                      .doFinally(s -> subscriptionQueryResult.close())
                                      .map(BikeStatus::description)
                                      .map(description -> ServerSentEvent.builder(description).build());
    }

    @PostMapping("/requestBike")
    public CompletableFuture<String> requestBike(@RequestParam("bikeId") String bikeId,
                                                 @RequestParam(value = "renter", required = false) String renter) {
        return commandGateway.send(new RequestBikeCommand(bikeId, renter == null ? randomRenter() : renter));
    }

    @PostMapping("/returnBike")
    public CompletableFuture<String> returnBike(@RequestParam("bikeId") String bikeId) {
        return commandGateway.send(new ReturnBikeCommand(bikeId, randomLocation()));
    }

    @GetMapping("findPayment")
    public Mono<String> getPaymentId(@RequestParam("reference") String paymentRef) {
        SubscriptionQueryResult<String, String> queryResult = queryGateway.subscriptionQuery("getPaymentId", paymentRef, String.class, String.class);
        return queryResult.initialResult().concatWith(queryResult.updates())
                          .filter(Objects::nonNull)
                          .next()
                          .timeout(Duration.ofSeconds(5), Mono.just("No payment found for given rental reference"));

    }

    @GetMapping("pendingPayments")
    public CompletableFuture<PaymentStatus> getPendingPayments() {
        return queryGateway.query("getAllPayments", PaymentStatus.Status.PENDING, PaymentStatus.class);
    }

    @PostMapping("acceptPayment")
    public CompletableFuture<Void> acceptPayment(@RequestParam("id") String paymentId) {
        return commandGateway.send(new ConfirmPaymentCommand(paymentId));
    }

    @GetMapping(value = "watch", produces = "text/event-stream")
    public Flux<String> watchAll() {
        SubscriptionQueryResult<List<BikeStatus>, BikeStatus> subscriptionQuery = queryGateway.subscriptionQuery(FIND_ALL_QUERY, null, ResponseTypes.multipleInstancesOf(BikeStatus.class), ResponseTypes.instanceOf(BikeStatus.class));
        return subscriptionQuery.initialResult()
                                .flatMapMany(Flux::fromIterable)
                                .concatWith(subscriptionQuery.updates())
                                .map(bs -> bs.getBikeId() + " -> " + bs.description());
    }

    @GetMapping(value = "watch/{bikeId}", produces = "text/event-stream")
    public Flux<String> watchBike(@PathVariable("bikeId") String bikeId) {
        SubscriptionQueryResult<BikeStatus, BikeStatus> subscriptionQuery = queryGateway.subscriptionQuery(FIND_ONE_QUERY, bikeId, ResponseTypes.instanceOf(BikeStatus.class), ResponseTypes.instanceOf(BikeStatus.class));
        return subscriptionQuery.initialResult()
                                .concatWith(subscriptionQuery.updates())
                                .map(bs -> bs.getBikeId() + " -> " + bs.description());
    }


    @PostMapping(value = "/rentPayReturn")
    public Mono<String> generateData(@RequestParam(value = "bikeId") String bikeId,
                                     @RequestParam(value = "renter", defaultValue = "John Doe") String renter) {
        return Mono.fromFuture(() -> rentPayAndReturn(renter, bikeId));
    }

    @PostMapping(value = "/generateRentals")
    public Flux<String> generateData(@RequestParam(value = "bikeType", defaultValue = "city") String bikeType,
                                     @RequestParam(value = "loops", defaultValue = "1") int loops,
                                     @RequestParam(value = "concurrency", defaultValue = "1") int concurrency) {
        return Flux.range(0, loops)
                   .flatMap(j -> executeRentalCycle(bikeType, randomRenter()).map(r -> "OK - Rented, Payed and Returned\n")
                                                                             .onErrorResume(e -> Mono.just("Not ok: " + e.getMessage() + "\n")),
                            concurrency);
    }

    @GetMapping("/bikes/{bikeId}")
    public CompletableFuture<BikeStatus> findStatus(@PathVariable("bikeId") String bikeId) {
        return queryGateway.query(FIND_ONE_QUERY, bikeId, BikeStatus.class);
    }

    private Mono<String> executeRentalCycle(String bikeType, String renter) {
        CompletableFuture<String> result = selectRandomAvailableBike(bikeType)
                .thenComposeAsync(bikeId -> rentPayAndReturn(renter, bikeId));
        return Mono.fromFuture(result);
    }

    private CompletableFuture<String> rentPayAndReturn(String renter, String bikeId) {
        return commandGateway.send(new RequestBikeCommand(bikeId, renter))
                             .thenComposeAsync(paymentRef -> executePayment(bikeId, (String) paymentRef))
                             .thenComposeAsync(r -> whenBikeUnlocked(bikeId))
                             .thenComposeAsync(r -> commandGateway.send(new ReturnBikeCommand(bikeId, randomLocation())))
                             .thenApply(r -> bikeId);
    }

    private CompletableFuture<String> selectRandomAvailableBike(String bikeType) {
        return queryGateway.query(FIND_AVAILABLE, bikeType, ResponseTypes.multipleInstancesOf(BikeStatus.class))
                           .thenApply(this::pickRandom)
                           .thenApply(BikeStatus::getBikeId);
    }

    private <T> T pickRandom(List<T> source) {
        return source.get(ThreadLocalRandom.current().nextInt(source.size()));
    }

    private CompletableFuture<String> whenBikeUnlocked(String bikeId) {
        SubscriptionQueryResult<BikeStatus, BikeStatus> queryResult = queryGateway.subscriptionQuery(FIND_ONE_QUERY, bikeId, BikeStatus.class, BikeStatus.class);
        return queryResult.initialResult().concatWith(queryResult.updates())
                          .any(status -> status.getStatus() == RentalStatus.RENTED)
                          .map(s -> bikeId)
                          .doOnNext(n -> queryResult.close())
                          .toFuture();
    }

    private CompletableFuture<String> executePayment(String bikeId, String paymentRef) {
        SubscriptionQueryResult<String, String> queryResult = queryGateway.subscriptionQuery("getPaymentId", paymentRef, String.class, String.class);
        return queryResult.initialResult().concatWith(queryResult.updates())
                          .filter(Objects::nonNull)
                          .doOnNext(n -> queryResult.close())
                          .next()
                          .flatMap(paymentId -> Mono.fromFuture(commandGateway.send(new ConfirmPaymentCommand(paymentId))))
                          .map(o -> bikeId)
                          .toFuture();
    }

    private String randomRenter() {
        return RENTERS.get(ThreadLocalRandom.current().nextInt(RENTERS.size()));
    }

    private String randomLocation() {
        return LOCATIONS.get(ThreadLocalRandom.current().nextInt(LOCATIONS.size()));
    }

}
