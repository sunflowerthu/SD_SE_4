package payments.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "inbox_messages")
data class InboxEntity(
    @Id
    @Column(name = "id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, unique = true)
    val messageId: String,

    @Column(nullable = false, length = 36)
    val aggregateId: String,

    @Column(nullable = false)
    val aggregateType: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,

    @Column(nullable = false)
    val eventType: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: InboxStatus = InboxStatus.PENDING,

    @Column
    var processedAt: LocalDateTime? = null,

    @Column
    var errorMessage: String? = null,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val receivedAt: LocalDateTime = LocalDateTime.now()
)

enum class InboxStatus {
    PENDING, PROCESSED, FAILED
}