package orders.repository

import orders.entity.OutboxEntity
import orders.entity.OutboxStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface OutboxRepository : JpaRepository<OutboxEntity, String> {
    fun findByStatus(status: OutboxStatus): List<OutboxEntity>

    @Modifying
    @Transactional
    @Query("UPDATE OutboxEntity o SET o.status = :newStatus, o.processedAt = CURRENT_TIMESTAMP WHERE o.id = :id AND o.status = :oldStatus")
    fun updateStatus(
        @Param("id") id: String,
        @Param("oldStatus") oldStatus: OutboxStatus,
        @Param("newStatus") newStatus: OutboxStatus
    ): Int
}