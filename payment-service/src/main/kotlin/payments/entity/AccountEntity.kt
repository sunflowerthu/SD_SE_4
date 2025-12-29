package payments.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "accounts",
    uniqueConstraints = [UniqueConstraint(columnNames = ["userId"])]
)
data class AccountEntity(
    @Id
    @Column(name = "id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, unique = true)
    val userId: String,

    @Column(nullable = false)
    var balance: Double = 0.0,

    @Version
    @Column(nullable = false)
    val version: Long = 0L,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)