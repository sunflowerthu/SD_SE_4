package common.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val error: String? = null,
    val success: Boolean
)

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String,
    val timestamp: String = LocalDateTime.now().toString()
)

@Serializable
enum class OrderStatus {
    NEW, PROCESSING, FINISHED, CANCELLED, FAILED
}

@Serializable
enum class PaymentStatus {
    PENDING, SUCCESS, FAILED
}