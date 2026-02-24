package com.aegis.api_platform.config;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String USAGE_EXCHANGE = "usage.exchange";
    public static final String USAGE_QUEUE = "usage.queue";
    public static final String USAGE_ROUTING_KEY = "usage.event";

    public static final String USAGE_DLQ_EXCHANGE = "usage.dlq.exchange";
    public static final String USAGE_DLQ_QUEUE = "usage.dlq.queue";
    public static final String USAGE_DLQ_ROUTING_KEY = "usage.dlq";

    // ---------- MAIN EXCHANGE ----------
    @Bean
    public DirectExchange usageExchange() {
        return new DirectExchange(USAGE_EXCHANGE);
    }

    // ---------- DLQ EXCHANGE ----------
    @Bean
    public DirectExchange usageDlqExchange() {
        return new DirectExchange(USAGE_DLQ_EXCHANGE);
    }

    // ---------- MAIN QUEUE ----------
    @Bean
    public Queue usageQueue() {
        return QueueBuilder.durable(USAGE_QUEUE)
                .withArgument("x-dead-letter-exchange", USAGE_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", USAGE_DLQ_ROUTING_KEY)
                .build();
    }

    // ---------- DLQ QUEUE ----------
    @Bean
    public Queue usageDlqQueue() {
        return QueueBuilder.durable(USAGE_DLQ_QUEUE)
                .build();
    }

    // ---------- BINDINGS ----------
    @Bean
    public Binding usageBinding() {
        return BindingBuilder
                .bind(usageQueue())
                .to(usageExchange())
                .with(USAGE_ROUTING_KEY);
    }

    @Bean
    public Binding usageDlqBinding() {
        return BindingBuilder
                .bind(usageDlqQueue())
                .to(usageDlqExchange())
                .with(USAGE_DLQ_ROUTING_KEY);
    }

    // ---------- JSON CONVERTER ----------
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ---------- RABBIT TEMPLATE ----------
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // ---------- LISTENER FACTORY WITH RETRY ----------
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        // Important: do NOT requeue infinitely
        factory.setDefaultRequeueRejected(false);

        // Retry 3 times with exponential backoff
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .backOffOptions(1000, 2.0, 10000)
                        .recoverer((message, cause) -> {
                            throw new AmqpRejectAndDontRequeueException(cause);
                        })
                        .build()
        );

        return factory;
    }
}

