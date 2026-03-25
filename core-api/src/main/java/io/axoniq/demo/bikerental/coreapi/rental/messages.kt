package io.axoniq.demo.bikerental.coreapi.rental

import org.axonframework.eventsourcing.annotation.EventTag
import org.axonframework.messaging.commandhandling.annotation.Command
import org.axonframework.messaging.eventhandling.annotation.Event
import org.axonframework.messaging.queryhandling.annotation.Query
import org.axonframework.modelling.annotation.TargetEntityId


@Command(routingKey = "bikeId")
data class RegisterBikeCommand(
    @TargetEntityId val bikeId: String,
    val bikeType: String,
    val location: String
)

@Command(routingKey = "bikeId")
data class RequestBikeCommand(@TargetEntityId val bikeId: String, val renter: String)

@Command(routingKey = "bikeId")
data class ApproveRequestCommand(@TargetEntityId val bikeId: String, val renter: String)

@Command(routingKey = "bikeId")
data class RejectRequestCommand(@TargetEntityId val bikeId: String, val renter: String)

@Command(routingKey = "bikeId")
data class ReturnBikeCommand(@TargetEntityId val bikeId: String, val location: String)

@Event()
data class BikeRegisteredEvent(@EventTag(key = "bikeId") val bikeId: String, val bikeType: String, val location: String)
@Event
data class BikeRequestedEvent(
    @EventTag(key = "bikeId") val bikeId: String,
    val renter: String,
    val rentalReference: String
)

data class BikeInUseEvent(@EventTag(key = "bikeId") val bikeId: String, val renter: String)
data class RequestRejectedEvent(@EventTag(key = "bikeId") val bikeId: String)
data class BikeReturnedEvent(@EventTag(key = "bikeId") val bikeId: String, val location: String)

@Query
class FindAll

@Query
data class FindAvailable(val bikeType: String)

@Query
data class FindOne(val bikeId: String)