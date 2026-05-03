package br.aof.read_opendata_apis.application.service;

import br.aof.read_opendata_apis.application.config.WakeMessageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class WakeMessageSender {

    private static final String TWILIO_BASE_URL = "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";
    private final WakeMessageProperties properties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void sendWakeMessage() {
        if (!properties.isEnabled()) {
            log.info("Wake message dispatch is disabled.");
            return;
        }

        WakeMessageProperties.Twilio twilioConfig = properties.getTwilio();
        if (isBlank(twilioConfig.getAccountSid())
                || isBlank(twilioConfig.getAuthToken())
                || isBlank(twilioConfig.getFromNumber())
                || isBlank(properties.getRecipient())
                || isBlank(properties.getText())) {
            log.warn("Wake message not sent: missing Twilio or wake message configuration.");
            return;
        }

        String to = withChannelPrefix(properties.getRecipient(), twilioConfig.isUseWhatsapp());
        String from = withChannelPrefix(twilioConfig.getFromNumber(), twilioConfig.isUseWhatsapp());
        String requestBody = encode("To", to) + "&" + encode("From", from) + "&" + encode("Body", properties.getText());

        String endpoint = String.format(TWILIO_BASE_URL, twilioConfig.getAccountSid());
        String basicAuth = Base64.getEncoder()
                .encodeToString((twilioConfig.getAccountSid() + ":" + twilioConfig.getAuthToken())
                        .getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Wake message sent to {}.", properties.getRecipient());
                return;
            }

            log.error("Wake message failed with status {}. Response: {}", response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Wake message sending interrupted.", e);
        } catch (IOException e) {
            log.error("Wake message sending failed with I/O error.", e);
        }
    }

    private static String withChannelPrefix(String phoneNumber, boolean useWhatsapp) {
        if (!useWhatsapp) {
            return phoneNumber;
        }
        if (phoneNumber.startsWith("whatsapp:")) {
            return phoneNumber;
        }
        return "whatsapp:" + phoneNumber;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String encode(String key, String value) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
