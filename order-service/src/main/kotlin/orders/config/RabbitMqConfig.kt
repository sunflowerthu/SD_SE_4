package orders.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    @Bean
    fun orderCreatedExchange(): DirectExchange {
        return DirectExchange("order.created.exchange")
    }

    @Bean
    fun orderCreatedQueue(): Queue {
        return Queue("order.created.queue", true)
    }

    @Bean
    fun paymentStatusExchange(): DirectExchange {
        return DirectExchange("payment.status.exchange")
    }

    @Bean
    fun paymentStatusQueue(): Queue {
        return Queue("payment.status.queue", true)
    }

    @Bean
    fun orderCreatedBinding(): Binding {
        return BindingBuilder
            .bind(orderCreatedQueue())
            .to(orderCreatedExchange())
            .with("order.created.routing")
    }

    @Bean
    fun paymentStatusBinding(): Binding {
        return BindingBuilder
            .bind(paymentStatusQueue())
            .to(paymentStatusExchange())
            .with("payment.status.routing")
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = Jackson2JsonMessageConverter()
        return template
    }
}