package payments.controller

import common.dto.ApiResponse
import payments.dto.BalanceResponse
import payments.dto.DepositRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import payments.dto.AccountResponse
import payments.dto.CreateAccountRequest
import payments.service.AccountService

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account management API")
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping
    @Operation(summary = "Create a new account")
    fun createAccount(
        @RequestBody request: CreateAccountRequest
    ): ResponseEntity<ApiResponse<AccountResponse>> {
        val response = accountService.createAccount(request)
        return ResponseEntity.status(
            if (response.success) HttpStatus.CREATED else HttpStatus.BAD_REQUEST
        ).body(response)
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money to account")
    fun deposit(
        @RequestBody request: DepositRequest
    ): ResponseEntity<ApiResponse<BalanceResponse>> {
        val response = accountService.deposit(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{userId}/balance")
    @Operation(summary = "Get account balance")
    fun getBalance(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<BalanceResponse>> {
        val response = accountService.getBalance(userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "payments-service",
            "timestamp" to System.currentTimeMillis().toString()
        ))
    }
}