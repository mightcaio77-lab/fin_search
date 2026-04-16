package br.aof.read_opendata_apis.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "wakeup-message")
public class WakeUpMessageProperties {
    private boolean enabled = false;
    private String cron = "0 0 7 * * *";
    private String zone = "America/Sao_Paulo";
    private String to = "+5521976100589";
    private String from;
    private String body = "Bom dia! Hora de acordar!";
    private Twilio twilio = new Twilio();

    @Getter
    @Setter
    public static class Twilio {
        private String accountSid;
        private String authToken;
    }
}
