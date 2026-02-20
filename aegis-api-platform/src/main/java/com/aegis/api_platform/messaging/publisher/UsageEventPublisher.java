package com.aegis.api_platform.messaging.publisher;

import com.aegis.api_platform.config.RabbitConfig;
import com.aegis.api_platform.messaging.event.UsageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsageEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(UsageEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.USAGE_EXCHANGE,
                "usage.event",
                event
        );
    }
}