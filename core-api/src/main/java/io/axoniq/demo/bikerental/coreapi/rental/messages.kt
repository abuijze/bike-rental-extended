package io.axoniq.demo.bikerental.coreapi.rental

import org.axonframework.modelling.command.TargetAggregateIdentifier

data class RegisterBikeCommand(
    @TargetAggregateIdentifier val bikeId: String,
    val bikeType: String,
    val location: String
)

data class RequestBikeCommand(@TargetAggregateIdentifier val bikeId: String, val renter: String)
data class ApproveRequestCommand(@TargetAggregateIdentifier val bikeId: String, val renter: String)
data class RejectRequestCommand(@TargetAggregateIdentifier val bikeId: String, val renter: String)
data class ReturnBikeCommand(@TargetAggregateIdentifier val bikeId: String, val location: String)

data class BikeRegisteredEvent(val bikeId: String, val bikeType: String, val location: String)
data class BikeRequestedEvent(val bikeId: String, val renter: String, val rentalReference: String)
data class BikeInUseEvent(val bikeId: String, val renter: String)
data class RequestRejectedEvent(val bikeId: String)
data class BikeReturnedEvent(val bikeId: String, val location: String)
