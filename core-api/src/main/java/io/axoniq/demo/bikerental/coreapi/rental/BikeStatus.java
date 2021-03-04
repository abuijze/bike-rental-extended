package io.axoniq.demo.bikerental.coreapi.rental;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BikeStatus {

    @Id
    private String bikeId;
    private String bikeType;
    private String location;
    private String renter;
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
    }

    public String getRenter() {
        return renter;
    }

    public void requestedBy(String renter) {
        this.renter = renter;
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
                return String.format("Rented by %s in %s", renter, location);
            case AVAILABLE:
                return String.format("Available for rental in %s.", location);
            case REQUESTED:
                return String.format("Requested by %s in %s", renter, location);
            default:
                return "Status unknown";
        }
    }

    public String getBikeType() {
        return bikeType;
    }
}
