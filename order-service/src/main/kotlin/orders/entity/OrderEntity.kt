package orders.entity

import common.dto.OrderStatus
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "orders")
data class OrderEntity(
    @Id
    @Column(name = "id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val amount: Double,

    @Column(nullable = false)
    val description: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.NEW,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)