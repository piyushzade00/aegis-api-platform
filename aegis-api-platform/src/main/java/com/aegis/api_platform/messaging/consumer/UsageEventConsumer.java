package com.aegis.api_platform.messaging.consumer;

import com.aegis.api_platform.config.RabbitConfig;
import com.aegis.api_platform.messaging.event.UsageEvent;
import com.aegis.api_platform.model.UsageLog;
import com.aegis.api_platform.repository.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsageEventConsumer {

    private final UsageLogRepository usageLogRepository;

    @RabbitListener(queues = RabbitConfig.USAGE_QUEUE)
    public void consume(UsageEvent event) {
        UsageLog log = new UsageLog(
                event.tenantId(),
                event.apiId(),
                event.apiKeyId(),
                event.statusCode(),
                event.latencyMs()
        );

        usageLogRepository.save(log);
    }
}
