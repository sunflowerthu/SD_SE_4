package payments.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import payments.entity.InboxEntity
import payments.entity.InboxStatus

@Repository
interface InboxRepository : JpaRepository<InboxEntity, String> {
    fun existsByMessageId(messageId: String): Boolean
    fun findByStatus(status: InboxStatus): List<InboxEntity>
}