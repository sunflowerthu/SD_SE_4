package payments.dto

import common.dto.PaymentStatus
import kotlinx.serialization.Serializable


@kotlinx.serialization.Serializable
data class CreateAccountRequest(
    val userId: String
)

@kotlinx.serialization.Serializable
data class AccountResponse(
    val id: String,
    val userId: String,
    val balance: Double,
    val createdAt: String
)

@kotlinx.serialization.Serializable
data class DepositRequest(
    val userId: String,
    val amount: Double
)

@kotlinx.serialization.Serializable
data class BalanceResponse(
    val userId: String,
    val balance: Double
)

@kotlinx.serialization.Serializable
data class OrderCreatedEvent(
    val orderId: String,
    val userId: String,
    val amount: Double,
    val description: String
)

@Serializable
data class PaymentResultEvent(
    val orderId: String,
    val userId: String,
    val status: PaymentStatus,
    val message: String? = null
)