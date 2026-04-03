package com.aegis.api_platform.ai.agent.alert;

import com.aegis.api_platform.config.RabbitConfig;
import com.aegis.api_platform.model.AlertLog;
import com.aegis.api_platform.repository.AlertLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertConsumer {

    private final AlertLogRepository alertLogRepository;

    @RabbitListener(queues = RabbitConfig.ALERT_QUEUE,
            containerFactory = "rabbitListenerContainerFactory")
    public void consume(AlertEvent event) {

        log.warn("ALERT [{}] Tenant {} - {} (severity: {}) - {}",
                event.alertType(),
                event.tenantId(),
                event.alertType(),
                event.severityScore(),
                event.message()
        );

        AlertLog alertLog = new AlertLog(
                event.alertId(),
                event.tenantId(),
                event.alertType(),
                event.message(),
                event.severityScore()
        );

        alertLogRepository.save(alertLog);
    }
}
