package orders.service

import com.fasterxml.jackson.databind.ObjectMapper
import orders.dto.OrderCreatedEvent
import orders.entity.OutboxStatus
import orders.repository.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutboxProcessor(
    private val outboxRepository: OutboxRepository,
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(OutboxProcessor::class.java)

    @Scheduled(fixedDelay = 5000)
    @Transactional
    fun processOutboxMessages() {
        val pendingMessages = outboxRepository.findByStatus(OutboxStatus.PENDING)

        log.info("Processing {} pending outbox messages", pendingMessages.size)

        for (message in pendingMessages) {
            try {
                when (message.eventType) {
                    "ORDER_CREATED" -> {
                        val event = objectMapper.readValue(message.payload, OrderCreatedEvent::class.java)

                        rabbitTemplate.convertAndSend(
                            "order.created.exchange",
                            "order.created.routing",
                            event
                        )

                        val updated = outboxRepository.updateStatus(
                            message.id,
                            OutboxStatus.PENDING,
                            OutboxStatus.PROCESSED
                        )

                        if (updated > 0) {
                            log.info("Successfully processed outbox message: {}", message.id)
                        }
                    }
                    else -> {
                        log.warn("Unknown event type: {}", message.eventType)
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to process outbox message: {}", message.id, e)
                outboxRepository.updateStatus(
                    message.id,
                    OutboxStatus.PENDING,
                    OutboxStatus.FAILED
                )
            }
        }
    }
}