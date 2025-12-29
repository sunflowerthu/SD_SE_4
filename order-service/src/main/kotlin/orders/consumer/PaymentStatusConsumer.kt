package orders.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import common.dto.OrderStatus
import common.dto.PaymentStatus
import orders.dto.PaymentStatusEvent
import orders.service.OrderService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class PaymentStatusConsumer(
    private val orderService: OrderService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(PaymentStatusConsumer::class.java)

    @RabbitListener(queues = ["payment.status.queue"])
    fun handlePaymentStatus(message: String) {
        try {
            val event = objectMapper.readValue(message, PaymentStatusEvent::class.java)
            log.info("Received payment status: {} for order: {}", event.status, event.orderId)

            when (event.status) {
                PaymentStatus.SUCCESS -> {
                    orderService.updateOrderStatus(event.orderId, OrderStatus.FINISHED)
                }
                PaymentStatus.FAILED -> {
                    orderService.updateOrderStatus(event.orderId, OrderStatus.CANCELLED)
                }
                PaymentStatus.PENDING -> { }
            }
        } catch (e: Exception) {
            log.error("Failed to process payment status message", e)
        }
    }
}