package br.aof.read_opendata_apis.scheduler;

import br.aof.read_opendata_apis.application.service.WakeMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WakeMessageScheduler {

    private final WakeMessageSender wakeMessageSender;

    @Scheduled(
            cron = "${wake.message.cron:0 0 7 * * *}",
            zone = "${wake.message.zone:America/Sao_Paulo}"
    )
    public void sendWakeMessageJob() {
        log.info("Starting wake message scheduled job.");
        wakeMessageSender.sendWakeMessage();
        log.info("Finishing wake message scheduled job.");
    }
}
