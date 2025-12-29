package payments.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import payments.dto.OrderCreatedEvent
import payments.entity.InboxEntity
import payments.entity.InboxStatus
import payments.repository.InboxRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class OrderCreatedConsumer(
    private val inboxRepository: InboxRepository,
    private val objectMapper: ObjectMapper,
    private val entityManager: EntityManager
) {
    private val log = LoggerFactory.getLogger(OrderCreatedConsumer::class.java)

    @RabbitListener(queues = ["order.created.queue"])
    @Transactional
    fun handleOrderCreated(message: String) {
        try {
            val event = objectMapper.readValue(message, OrderCreatedEvent::class.java)
            val messageId = UUID.randomUUID().toString()

            log.info("Received order created event: {} for user: {}", event.orderId, event.userId)

            if (inboxRepository.existsByMessageId(messageId)) {
                log.info("Message already processed: {}", messageId)
                return
            }

            val inboxMessage = InboxEntity(
                messageId = messageId,
                aggregateId = event.orderId,
                aggregateType = "ORDER",
                payload = objectMapper.writeValueAsString(event),
                eventType = "ORDER_CREATED",
                status = InboxStatus.PENDING
            )

            inboxRepository.save(inboxMessage)
            entityManager.flush()

            log.info("Order created event saved to inbox: {}", event.orderId)
        } catch (e: Exception) {
            log.error("Failed to process order created message", e)
            throw e
        }
    }
}