package br.aof.read_opendata_apis.application.service;

import br.aof.read_opendata_apis.application.config.WakeUpMessageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WakeUpMessageService {
    private final WakeUpMessageProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public void sendWakeUpMessage() {
        validateConfiguration();
        String accountSid = properties.getTwilio().getAccountSid();
        String authToken = properties.getTwilio().getAuthToken();
        String endpoint = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, buildBasicAuthenticationHeader(accountSid, authToken));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("To", properties.getTo());
        body.add("From", properties.getFrom());
        body.add("Body", properties.getBody());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
            HttpStatusCode status = response.getStatusCode();
            if (status.is2xxSuccessful()) {
                log.info("Wake-up message sent to {}", properties.getTo());
            } else {
                log.error("Twilio returned non-success status {} while sending wake-up message", status.value());
            }
        } catch (HttpStatusCodeException ex) {
            log.error(
                    "Twilio request failed with status {} and response {}",
                    ex.getStatusCode().value(),
                    ex.getResponseBodyAsString(),
                    ex
            );
        } catch (RestClientException ex) {
            log.error("Failed to send wake-up message due to client error", ex);
        }
    }

    private void validateConfiguration() {
        List<String> missingConfigs = new ArrayList<>();
        if (isBlank(properties.getTwilio().getAccountSid())) {
            missingConfigs.add("wakeup-message.twilio.account-sid");
        }
        if (isBlank(properties.getTwilio().getAuthToken())) {
            missingConfigs.add("wakeup-message.twilio.auth-token");
        }
        if (isBlank(properties.getFrom())) {
            missingConfigs.add("wakeup-message.from");
        }
        if (isBlank(properties.getTo())) {
            missingConfigs.add("wakeup-message.to");
        }
        if (isBlank(properties.getBody())) {
            missingConfigs.add("wakeup-message.body");
        }

        if (!missingConfigs.isEmpty()) {
            throw new IllegalStateException("Missing wake-up message configuration: " + String.join(", ", missingConfigs));
        }
    }

    private String buildBasicAuthenticationHeader(String username, String password) {
        String credentials = username + ":" + password;
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + base64Credentials;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
