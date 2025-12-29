package orders.repository

import orders.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<OrderEntity, String> {
    fun findByUserId(userId: String): List<OrderEntity>
}