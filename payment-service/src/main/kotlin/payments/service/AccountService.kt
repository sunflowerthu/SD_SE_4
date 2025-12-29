package payments.service

import common.dto.ApiResponse
import payments.dto.AccountResponse
import payments.dto.BalanceResponse
import payments.dto.CreateAccountRequest
import payments.dto.DepositRequest
import payments.entity.AccountEntity
import payments.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {
    private val log = LoggerFactory.getLogger(AccountService::class.java)

    @Transactional
    fun createAccount(request: CreateAccountRequest): ApiResponse<AccountResponse> {
        return try {
            val existingAccount = accountRepository.findByUserId(request.userId)
            if (existingAccount.isPresent) {
                return ApiResponse(
                    error = "Account already exists for user: ${request.userId}",
                    success = false
                )
            }

            val account = AccountEntity(userId = request.userId)
            val savedAccount = accountRepository.save(account)

            log.info("Account created for user: {}", request.userId)

            ApiResponse(
                data = AccountResponse(
                    id = savedAccount.id,
                    userId = savedAccount.userId,
                    balance = savedAccount.balance,
                    createdAt = savedAccount.createdAt.toString()
                ),
                success = true
            )
        } catch (e: Exception) {
            log.error("Failed to create account for user: {}", request.userId, e)
            ApiResponse(error = "Failed to create account: ${e.message}", success = false)
        }
    }

    @Transactional
    fun deposit(request: DepositRequest): ApiResponse<BalanceResponse> {
        return try {
            val account = accountRepository.findByUserId(request.userId)
                .orElseThrow { RuntimeException("Account not found for user: ${request.userId}") }

            val updatedRows = accountRepository.deposit(request.userId, request.amount)

            if (updatedRows > 0) {
                log.info("Deposited {} to account of user: {}", request.amount, request.userId)

                val updatedAccount = accountRepository.findByUserId(request.userId)
                    .orElseThrow { RuntimeException("Failed to get updated account") }

                ApiResponse(
                    data = BalanceResponse(
                        userId = updatedAccount.userId,
                        balance = updatedAccount.balance
                    ),
                    success = true
                )
            } else {
                ApiResponse(error = "Failed to deposit", success = false)
            }
        } catch (e: Exception) {
            log.error("Failed to deposit for user: {}", request.userId, e)
            ApiResponse(error = "Failed to deposit: ${e.message}", success = false)
        }
    }

    fun getBalance(userId: String): ApiResponse<BalanceResponse> {
        return try {
            val account = accountRepository.findByUserId(userId)
                .orElseThrow { RuntimeException("Account not found for user: $userId") }

            ApiResponse(
                data = BalanceResponse(
                    userId = account.userId,
                    balance = account.balance
                ),
                success = true
            )
        } catch (e: Exception) {
            log.error("Failed to get balance for user: {}", userId, e)
            ApiResponse(error = "Failed to get balance: ${e.message}", success = false)
        }
    }

    @Transactional
    fun processPayment(userId: String, amount: Double): Boolean {
        return try {
            val account = accountRepository.findByUserId(userId)
            if (account.isEmpty) {
                log.warn("Account not found for user: {}", userId)
                return false
            }

            val updatedRows = accountRepository.withdrawIfSufficientBalance(userId, amount)

            if (updatedRows > 0) {
                log.info("Payment processed: {} from user: {}", amount, userId)
                true
            } else {
                log.warn("Insufficient balance for user: {}", userId)
                false
            }
        } catch (e: Exception) {
            log.error("Failed to process payment for user: {}", userId, e)
            false
        }
    }
}