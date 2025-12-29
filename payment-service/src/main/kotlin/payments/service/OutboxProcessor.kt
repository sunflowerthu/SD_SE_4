package payments.service

import com.fasterxml.jackson.databind.ObjectMapper
import payments.dto.PaymentResultEvent
import payments.entity.OutboxStatus
import payments.repository.OutboxRepository
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
                    "PAYMENT_RESULT" -> {
                        val event = objectMapper.readValue(message.payload, PaymentResultEvent::class.java)

                        rabbitTemplate.convertAndSend(
                            "payment.status.exchange",
                            "payment.status.routing",
                            event
                        )

                        message.status = OutboxStatus.PROCESSED
                        message.processedAt = java.time.LocalDateTime.now()
                        outboxRepository.save(message)

                        log.info("Successfully sent payment result for order: {}", event.orderId)
                    }
                    else -> {
                        log.warn("Unknown event type: {}", message.eventType)
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to process outbox message: {}", message.id, e)
            }
        }
    }
}