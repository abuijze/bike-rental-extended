package io.axoniq.demo.bikerental.coreapi.rental;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.axonframework.messaging.queryhandling.annotation.QueryResponse;

@QueryResponse
@Entity
public class BikeStatus {

    @Id
    private String bikeId;
    private String bikeType;
    private String location;
    private String renter;
    private String rentalReference;
    private RentalStatus status;

    public BikeStatus() {
    }

    public BikeStatus(String bikeId, String bikeType, String location) {
        this.bikeId = bikeId;
        this.bikeType = bikeType;
        this.location = location;
        this.status = RentalStatus.AVAILABLE;
    }

    public String getBikeId() {
        return bikeId;
    }

    public String getLocation() {
        return location;
    }

    public void returnedAt(String location) {
        this.location = location;
        this.status = RentalStatus.AVAILABLE;
        this.renter = null;
        this.rentalReference = null;
    }

    public String getRenter() {
        return renter;
    }

    public void requestedBy(String renter, String rentalReference) {
        this.renter = renter;
        this.rentalReference = rentalReference;
        this.status = RentalStatus.REQUESTED;
    }

    public void rentedBy(String renter) {
        this.renter = renter;
        this.status = RentalStatus.RENTED;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public String description() {
        switch (status) {
            case RENTED:
                return String.format("Bike %s was rented by %s in %s", bikeId, renter, location);
            case AVAILABLE:
                return String.format("Bike %s is available for rental in %s.", bikeId, location);
            case REQUESTED:
                return String.format("Bike %s is requested by %s in %s", bikeId, renter, location);
            default:
                return "Status unknown";
        }
    }

    public String getRentalReference() {
        return rentalReference;
    }

    public String getBikeType() {
        return bikeType;
    }
}
