package orders.controller

import common.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import orders.dto.CreateOrderRequest
import orders.dto.OrderResponse
import orders.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management API")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    @Operation(summary = "Create a new order")
    fun createOrder(
        @RequestBody request: CreateOrderRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val response = orderService.createOrder(request)
        return ResponseEntity.status(
            if (response.success) HttpStatus.CREATED else HttpStatus.BAD_REQUEST
        ).body(response)
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all orders for user")
    fun getOrdersByUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<List<OrderResponse>>> {
        val response = orderService.getOrdersByUser(userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    fun getOrder(
        @Parameter(description = "Order ID", required = true)
        @PathVariable orderId: String
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val response = orderService.getOrderById(orderId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "orders-service",
            "timestamp" to System.currentTimeMillis().toString()
        ))
    }
}