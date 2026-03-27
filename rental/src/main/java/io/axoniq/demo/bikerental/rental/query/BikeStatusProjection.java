package io.axoniq.demo.bikerental.rental.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.axoniq.demo.bikerental.coreapi.rental.*;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.queryhandling.QueryUpdateEmitter;
import org.axonframework.messaging.queryhandling.annotation.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class BikeStatusProjection {

    private final BikeStatusRepository bikeStatusRepository;

    public BikeStatusProjection(BikeStatusRepository bikeStatusRepository) {
        this.bikeStatusRepository = bikeStatusRepository;
    }

    @EventHandler
    public void on(BikeRegisteredEvent event, QueryUpdateEmitter updateEmitter) {
        var bikeStatus = new BikeStatus(event.bikeId(), event.bikeType(), event.location());
        bikeStatusRepository.save(bikeStatus);
        updateEmitter.emit(FindAll.class, q -> true, bikeStatus);
    }

    @EventHandler(eventName = "io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent")
    public void on(Map<String, Object> event, QueryUpdateEmitter updateEmitter) {
        bikeStatusRepository.findById((String) event.get("bikeId"))
                            .map(bs -> {
                                bs.requestedBy((String) event.get("renter"));
                                return bs;
                            })
                            .ifPresent(bs -> {
                                updateEmitter.emit(FindAll.class, q -> true, bs);
                                updateEmitter.emit(FindOne.class, q -> Objects.equals(q.bikeId(), event.get("bikeId")), bs);
                            });
    }

    @EventHandler
    public void on(BikeInUseEvent event, QueryUpdateEmitter updateEmitter) {
        bikeStatusRepository.findById(event.bikeId())
                            .map(bs -> {
                                bs.rentedBy(event.renter());
                                return bs;
                            })
                            .ifPresent(bs -> {
                                updateEmitter.emit(FindAll.class, q -> true, bs);
                                updateEmitter.emit(FindOne.class, q -> q.bikeId().equals(event.bikeId()), bs);
                            });
    }

    @EventHandler
    public void on(BikeReturnedEvent event, QueryUpdateEmitter updateEmitter) {
        bikeStatusRepository.findById(event.bikeId())
                            .map(bs -> {
                                bs.returnedAt(event.location());
                                return bs;
                            })
                            .ifPresent(bs -> {
                                updateEmitter.emit(FindAll.class, q -> true, bs);
                                updateEmitter.emit(FindOne.class, q -> q.bikeId().equals(event.bikeId()), bs);
                            });

    }

    @EventHandler
    public void on(RequestRejectedEvent event, QueryUpdateEmitter updateEmitter) {
        bikeStatusRepository.findById(event.bikeId())
                            .map(bs -> {
                                bs.returnedAt(bs.getLocation());
                                return bs;
                            })
                            .ifPresent(bs -> {
                                updateEmitter.emit(FindAll.class, q -> true, bs);
                                updateEmitter.emit(FindOne.class, q -> q.bikeId().equals(event.bikeId()), bs);
                            });
    }

    @QueryHandler
    public Iterable<BikeStatus> findAll(FindAll ignored) {
        return bikeStatusRepository.findAll();
    }

    @QueryHandler
    public Iterable<BikeStatus> findAvailable(FindAvailable query) {
        return bikeStatusRepository.findAllByBikeTypeAndStatus(query.bikeType(), RentalStatus.AVAILABLE);
    }

    @QueryHandler
    public BikeStatus findOne(FindOne query) {
        return bikeStatusRepository.findById(query.bikeId()).orElse(null);
    }

public static class BikeRequestedEventV2 {

    private final String bikeId;
    private final String renter;

    @JsonCreator
    public BikeRequestedEventV2(@JsonProperty("bikeId") String bikeId, @JsonProperty("renter") String renter) {
        this.bikeId = bikeId;
        this.renter = renter;
    }

    public String getBikeId() {
        return bikeId;
    }

    public String getRenter() {
        return renter;
    }
}
}
