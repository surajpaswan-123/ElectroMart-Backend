package electromart.ElectroMart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from-email:}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendOtpEmail(String email, String otp) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY is not configured");
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("RESEND_FROM_EMAIL is not configured");
        }

        String subject = "ElectroMart - Email Verification OTP";
        String text = "Your ElectroMart verification OTP is: " + otp
                + "\n\nThis OTP expires in 10 minutes."
                + "\nIf you did not create an ElectroMart account, you can ignore this email.";

        String html = "<div style=\"font-family:Arial,sans-serif;max-width:520px;margin:auto\">"
                + "<h2>ElectroMart Email Verification</h2>"
                + "<p>Your verification OTP is:</p>"
                + "<div style=\"font-size:32px;font-weight:700;letter-spacing:8px;margin:20px 0\">"
                + otp + "</div>"
                + "<p>This OTP expires in <strong>10 minutes</strong>.</p>"
                + "<p>If you did not create an ElectroMart account, you can ignore this email.</p>"
                + "</div>";

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "from", fromEmail,
                    "to", new String[]{email},
                    "subject", subject,
                    "text", text,
                    "html", html
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "ElectroMart/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Resend API returned HTTP " + response.statusCode() + ": " + response.body()
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Email request was interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send OTP email through Resend", e);
        }
    }
}
