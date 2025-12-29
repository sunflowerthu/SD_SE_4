package payments.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import payments.entity.OutboxEntity
import payments.entity.OutboxStatus

@Repository
interface OutboxRepository : JpaRepository<OutboxEntity, String> {
    fun findByStatus(status: OutboxStatus): List<OutboxEntity>
}