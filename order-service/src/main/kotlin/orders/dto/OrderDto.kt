package orders.dto

import common.dto.OrderStatus
import common.dto.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val userId: String,
    val amount: Double,
    val description: String
)

@Serializable
data class OrderResponse(
    val id: String,
    val userId: String,
    val amount: Double,
    val description: String,
    val status: OrderStatus,
    val createdAt: String
)

@Serializable
data class OrderCreatedEvent(
    val orderId: String,
    val userId: String,
    val amount: Double,
    val description: String
)

@Serializable
data class PaymentStatusEvent(
    val orderId: String,
    val userId: String,
    val status: PaymentStatus,
    val message: String? = null
)