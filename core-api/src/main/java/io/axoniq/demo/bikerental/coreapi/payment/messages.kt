package io.axoniq.demo.bikerental.coreapi.payment

import org.axonframework.eventsourcing.annotation.EventTag
import org.axonframework.messaging.commandhandling.annotation.Command
import org.axonframework.messaging.queryhandling.annotation.Query
import org.axonframework.modelling.annotation.TargetEntityId

@Command(routingKey = "paymentReference")
data class PreparePaymentCommand(val amount: Int, val paymentReference: String)

@Command(routingKey = "paymentId")
data class ConfirmPaymentCommand(@TargetEntityId val paymentId: String)

@Command(routingKey = "paymentId")
data class RejectPaymentCommand(@TargetEntityId val paymentId: String)

//@Event
data class PaymentPreparedEvent(
    @EventTag(key = "Payment") val paymentId: String,
    val amount: Int,
    val paymentReference: String
)

//@Event
data class PaymentConfirmedEvent(@EventTag(key = "Payment") val paymentId: String, val paymentReference: String)

//@Event
data class PaymentRejectedEvent(@EventTag(key = "Payment") val paymentId: String, val paymentReference: String)

@Query
data class GetPaymentIdQuery(val paymentReference: String)

@Query
data class GetAllPaymentsQuery(val paymentStatus: PaymentStatus.Status)