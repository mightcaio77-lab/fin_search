package br.aof.read_opendata_apis.scheduler;

import br.aof.read_opendata_apis.application.config.WakeUpMessageProperties;
import br.aof.read_opendata_apis.application.service.WakeUpMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WakeUpMessageScheduler {
    private final WakeUpMessageProperties properties;
    private final WakeUpMessageService wakeUpMessageService;

    @Scheduled(
            cron = "${wakeup-message.cron:0 0 7 * * *}",
            zone = "${wakeup-message.zone:America/Sao_Paulo}"
    )
    public void sendWakeUpMessage() {
        if (!properties.isEnabled()) {
            return;
        }

        log.info("Starting wake-up message job");
        try {
            wakeUpMessageService.sendWakeUpMessage();
        } catch (Exception ex) {
            log.error("Wake-up message job failed", ex);
        }
        log.info("Finishing wake-up message job");
    }
}
