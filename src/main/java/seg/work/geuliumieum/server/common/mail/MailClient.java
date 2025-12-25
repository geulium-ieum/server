package seg.work.geuliumieum.server.common.mail;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${hermes.sendTemplateUrl}")
    private String sendTemplateUrl;

    @Value("${hermes.groupKey}")
    private String groupKey;

    public void sendVerificationEmail(String to, String name, String verificationUrl, String code, int expiryMinutes) {
        Map<String, Object> body = new HashMap<>();
        body.put("groupKey", groupKey);
        body.put("to", to);
        body.put("templateName", "verification");
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", name);
        vars.put("company", "그리움 이음");
        vars.put("verification_url", verificationUrl);
        vars.put("verification_code", code);
        vars.put("expiry_minutes", expiryMinutes);
        body.put("variables", vars);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(sendTemplateUrl, new HttpEntity<>(body, headers), Void.class);
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String name, String resetUrl, String code, int expiryMinutes) {
        Map<String, Object> body = new HashMap<>();
        body.put("groupKey", groupKey);
        body.put("to", to);
        body.put("templateName", "password_reset");
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", name);
        vars.put("company", "그리움 이음");
        vars.put("reset_url", resetUrl);
        vars.put("reset_code", code);
        vars.put("expiry_minutes", expiryMinutes);
        body.put("variables", vars);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(sendTemplateUrl, new HttpEntity<>(body, headers), Void.class);
        } catch (Exception e) {
            log.warn("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }
}
