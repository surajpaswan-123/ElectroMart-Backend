package electromart.ElectroMart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("ElectroMart - Email Verification OTP");
        message.setText("Your OTP is: " + otp + "\n\nThis OTP expires in 10 minutes.");

        try {
            log.info("Sending OTP email to {}", maskEmail(email));
            mailSender.send(message);
            log.info("OTP email sent successfully to {}", maskEmail(email));
        } catch (Exception e) {
            log.error("OTP email delivery failed for {}. SMTP error type: {}, message: {}",
                    maskEmail(email), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "unknown";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(at, 0));
        return email.charAt(0) + "***" + email.substring(at);
    }
}
