package payments.service

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import common.dto.PaymentStatus
import payments.dto.OrderCreatedEvent
import payments.dto.PaymentResultEvent
import payments.entity.InboxEntity
import payments.entity.InboxStatus
import payments.entity.OutboxEntity
import payments.repository.AccountRepository
import payments.repository.InboxRepository
import payments.repository.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InboxProcessor(
    private val inboxRepository: InboxRepository,
    private val outboxRepository: OutboxRepository,
    private val accountRepository: AccountRepository,
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(InboxProcessor::class.java)

    @Scheduled(fixedDelay = 5000)
    @Transactional
    fun processInboxMessages() {
        val pendingMessages = inboxRepository.findByStatus(InboxStatus.PENDING)

        log.info("Processing {} pending inbox messages", pendingMessages.size)

        for (message in pendingMessages) {
            try {
                when (message.eventType) {
                    "ORDER_CREATED" -> {
                        val event = objectMapper.readValue(message.payload, OrderCreatedEvent::class.java)
                        processOrderCreated(event, message)
                    }
                    else -> {
                        log.warn("Unknown event type: {}", message.eventType)
                        message.status = InboxStatus.FAILED
                        message.errorMessage = "Unknown event type"
                        inboxRepository.save(message)
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to process inbox message: {}", message.id, e)
                message.status = InboxStatus.FAILED
                message.errorMessage = e.message
                inboxRepository.save(message)
            }
        }
    }

    private fun processOrderCreated(event: OrderCreatedEvent, inboxMessage: InboxEntity) {
        log.info("Processing order created event: {} for user: {}", event.orderId, event.userId)

        val account = accountRepository.findByUserId(event.userId)
        if (account.isEmpty) {
            createPaymentResultEvent(
                orderId = event.orderId,
                userId = event.userId,
                status = PaymentStatus.FAILED,
                message = "Account not found"
            )
            inboxMessage.status = InboxStatus.PROCESSED
            inboxMessage.processedAt = java.time.LocalDateTime.now()
            inboxRepository.save(inboxMessage)
            return
        }
        val paymentSuccessful = accountRepository.withdrawIfSufficientBalance(event.userId, event.amount) > 0

        if (paymentSuccessful) {
            createPaymentResultEvent(
                orderId = event.orderId,
                userId = event.userId,
                status = PaymentStatus.SUCCESS,
                message = "Payment successful"
            )
            log.info("Payment successful for order: {}", event.orderId)
        } else {
            createPaymentResultEvent(
                orderId = event.orderId,
                userId = event.userId,
                status = PaymentStatus.FAILED,
                message = "Insufficient balance"
            )
            log.warn("Insufficient balance for order: {}", event.orderId)
        }

        inboxMessage.status = InboxStatus.PROCESSED
        inboxMessage.processedAt = java.time.LocalDateTime.now()
        inboxRepository.save(inboxMessage)
    }

    private fun createPaymentResultEvent(
        orderId: String,
        userId: String,
        status: PaymentStatus,
        message: String? = null
    ) {
        val event = PaymentResultEvent(
            orderId = orderId,
            userId = userId,
            status = status,
            message = message
        )

        val outboxMessage = OutboxEntity(
            aggregateId = orderId,
            aggregateType = "PAYMENT",
            payload = objectMapper.writeValueAsString(event),
            eventType = "PAYMENT_RESULT"
        )

        outboxRepository.save(outboxMessage)
        entityManager.flush()
    }
}