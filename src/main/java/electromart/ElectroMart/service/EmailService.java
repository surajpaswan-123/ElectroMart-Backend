package electromart.ElectroMart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("ElectroMart - Email Verification OTP");
        message.setText("Your OTP is: " + otp + "\n\nThis OTP expires in 10 minutes.");
        mailSender.send(message);
    }
}
