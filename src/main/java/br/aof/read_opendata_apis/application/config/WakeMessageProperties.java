package br.aof.read_opendata_apis.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "wake.message")
public class WakeMessageProperties {
    private boolean enabled = true;
    private String recipient = "+5521976100589";
    private String text = "Bom dia! Hora de acordar.";
    private String cron = "0 0 7 * * *";
    private String zone = "America/Sao_Paulo";
    private Twilio twilio = new Twilio();

    @Getter
    @Setter
    public static class Twilio {
        private String accountSid = "";
        private String authToken = "";
        private String fromNumber = "";
        private boolean useWhatsapp = false;
    }
}
