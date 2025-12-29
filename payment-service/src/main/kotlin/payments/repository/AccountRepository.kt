package payments.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import payments.entity.AccountEntity
import java.util.*

@Repository
interface AccountRepository : JpaRepository<AccountEntity, String> {
    fun findByUserId(userId: String): Optional<AccountEntity>

    @Modifying
    @Transactional
    @Query("UPDATE AccountEntity a SET a.balance = a.balance + :amount WHERE a.userId = :userId")
    fun deposit(@Param("userId") userId: String, @Param("amount") amount: Double): Int

    @Modifying
    @Transactional
    @Query("""
        UPDATE AccountEntity a 
        SET a.balance = a.balance - :amount 
        WHERE a.userId = :userId AND a.balance >= :amount
    """)
    fun withdrawIfSufficientBalance(
        @Param("userId") userId: String,
        @Param("amount") amount: Double
    ): Int
}