package com.aegis.api_platform.messaging.consumer;

import com.aegis.api_platform.config.RabbitConfig;
import com.aegis.api_platform.messaging.event.UsageEvent;
import com.aegis.api_platform.model.UsageLog;
import com.aegis.api_platform.repository.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsageEventConsumer {

    private final UsageLogRepository usageLogRepository;

    @RabbitListener(queues = RabbitConfig.USAGE_QUEUE,
            containerFactory = "rabbitListenerContainerFactory")
    public void consume(UsageEvent event) {

        MDC.put("correlationId", event.correlationId());

        try {
            UsageLog usageLog = new UsageLog(
                    event.eventId(),
                    event.tenantId(),
                    event.apiId(),
                    event.apiKeyId(),
                    event.statusCode(),
                    event.latencyMs()
            );

            usageLogRepository.save(usageLog);
        }catch (DataIntegrityViolationException ex) {

            if (isDuplicateEvent(ex)) {
                // safe duplicate - ignore
                log.debug("Duplicate event ignored: {}", event.eventId());
            } else {
                log.error("Unexpected DB error while saving usage event {}", event.eventId(), ex);
                throw ex;
            }

        }finally {
            MDC.remove("correlationId");
        }
    }

    private boolean isDuplicateEvent(DataIntegrityViolationException ex) {
        return ex.getMessage() != null &&
                ex.getMessage().contains("uk_usage_event_id");
    }
}
