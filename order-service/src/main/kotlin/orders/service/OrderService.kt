package orders.service

import com.fasterxml.jackson.databind.ObjectMapper
import common.dto.ApiResponse
import common.dto.OrderStatus
import jakarta.persistence.EntityManager
import orders.dto.CreateOrderRequest
import orders.dto.OrderCreatedEvent
import orders.dto.OrderResponse
import orders.entity.OrderEntity
import orders.entity.OutboxEntity
import orders.repository.OrderRepository
import orders.repository.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val outboxRepository: OutboxRepository,
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(OrderService::class.java)

    @Transactional
    fun createOrder(request: CreateOrderRequest): ApiResponse<OrderResponse> {
        return try {
            // 1. Создаем заказ в рамках транзакции
            val order = OrderEntity(
                userId = request.userId,
                amount = request.amount,
                description = request.description
            )

            val savedOrder = orderRepository.save(order)

            // 2. Создаем сообщение для outbox в рамках той же транзакции
            val event = OrderCreatedEvent(
                orderId = savedOrder.id,
                userId = savedOrder.userId,
                amount = savedOrder.amount,
                description = savedOrder.description
            )

            val outboxMessage = OutboxEntity(
                aggregateId = savedOrder.id,
                aggregateType = "ORDER",
                payload = objectMapper.writeValueAsString(event),
                eventType = "ORDER_CREATED"
            )

            outboxRepository.save(outboxMessage)

            // Фиксируем транзакцию
            entityManager.flush()

            log.info("Order created: {} for user: {}", savedOrder.id, savedOrder.userId)

            ApiResponse(
                data = OrderResponse(
                    id = savedOrder.id,
                    userId = savedOrder.userId,
                    amount = savedOrder.amount,
                    description = savedOrder.description,
                    status = savedOrder.status,
                    createdAt = savedOrder.createdAt.toString()
                ),
                success = true
            )
        } catch (e: Exception) {
            log.error("Failed to create order", e)
            ApiResponse(error = "Failed to create order: ${e.message}", success = false)
        }
    }

    fun getOrdersByUser(userId: String): ApiResponse<List<OrderResponse>> {
        return try {
            val orders = orderRepository.findByUserId(userId)
            ApiResponse(
                data = orders.map { order ->
                    OrderResponse(
                        id = order.id,
                        userId = order.userId,
                        amount = order.amount,
                        description = order.description,
                        status = order.status,
                        createdAt = order.createdAt.toString()
                    )
                },
                success = true
            )
        } catch (e: Exception) {
            log.error("Failed to get orders for user: {}", userId, e)
            ApiResponse(error = "Failed to get orders: ${e.message}", success = false)
        }
    }

    fun getOrderById(orderId: String): ApiResponse<OrderResponse> {
        return try {
            val order = orderRepository.findById(orderId)
                .orElseThrow { RuntimeException("Order not found: $orderId") }

            ApiResponse(
                data = OrderResponse(
                    id = order.id,
                    userId = order.userId,
                    amount = order.amount,
                    description = order.description,
                    status = order.status,
                    createdAt = order.createdAt.toString()
                ),
                success = true
            )
        } catch (e: Exception) {
            log.error("Failed to get order: {}", orderId, e)
            ApiResponse(error = "Order not found: $orderId", success = false)
        }
    }

    @Transactional
    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        try {
            val order = orderRepository.findById(orderId)
                .orElseThrow { RuntimeException("Order not found: $orderId") }

            order.status = status
            orderRepository.save(order)

            log.info("Order {} status updated to {}", orderId, status)
        } catch (e: Exception) {
            log.error("Failed to update order status: {}", orderId, e)
        }
    }
}