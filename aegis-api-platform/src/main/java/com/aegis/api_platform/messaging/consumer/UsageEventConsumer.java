package com.aegis.api_platform.messaging.consumer;

import com.aegis.api_platform.config.RabbitConfig;
import com.aegis.api_platform.messaging.event.UsageEvent;
import com.aegis.api_platform.model.UsageLog;
import com.aegis.api_platform.repository.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsageEventConsumer {

    private final UsageLogRepository usageLogRepository;

    @RabbitListener(queues = RabbitConfig.USAGE_QUEUE,
            containerFactory = "rabbitListenerContainerFactory")
    public void consume(UsageEvent event) {

        MDC.put("correlationId", event.correlationId());

        try {
            UsageLog log = new UsageLog(
                    event.tenantId(),
                    event.apiId(),
                    event.apiKeyId(),
                    event.statusCode(),
                    event.latencyMs()
            );

            usageLogRepository.save(log);
        }finally {
            MDC.remove("correlationId");
        }
    }
}
