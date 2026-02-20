package com.aegis.api_platform.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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

    @Bean
    public TopicExchange usageExchange() {
        return new TopicExchange(USAGE_EXCHANGE);
    }

    @Bean
    public Queue usageQueue() {
        return new Queue(USAGE_QUEUE, true);
    }

    @Bean
    public Binding usageBinding(Queue usageQueue,
                                TopicExchange usageExchange) {
        return BindingBuilder
                .bind(usageQueue)
                .to(usageExchange)
                .with("usage.#");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}

