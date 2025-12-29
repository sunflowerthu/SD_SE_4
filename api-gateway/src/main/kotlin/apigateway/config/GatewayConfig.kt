package apigateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig {

    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("orders-service") { r ->
                r.path("/api/orders/**")
                    .uri("http://orders-service:8081")
            }
            .route("payments-service") { r ->
                r.path("/api/accounts/**")
                    .uri("http://payments-service:8082")
            }
            .route("orders-swagger") { r ->
                r.path("/swagger/orders/**")
                    .filters { f ->
                        f.rewritePath("/swagger/orders/(?<segment>.*)", "/\${segment}")
                    }
                    .uri("http://orders-service:8081")
            }
            .route("payments-swagger") { r ->
                r.path("/swagger/payments/**")
                    .filters { f ->
                        f.rewritePath("/swagger/payments/(?<segment>.*)", "/\${segment}")
                    }
                    .uri("http://payments-service:8082")
            }
            .build()
    }
}