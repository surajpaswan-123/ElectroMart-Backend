package electromart.ElectroMart.service;

import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class UserService {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return userRepository.findByEmail(normalizeEmail(email)).orElse(null);
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public User registerUser(User user) {
        user.setEmail(normalizeEmail(user.getEmail()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    public String hashOtp(String otp) {
        return passwordEncoder.encode(otp);
    }

    public boolean canResendOtp(User user) {
        if (user == null || user.getOtpLastSentAt() == null) return true;
        return !LocalDateTime.now().isBefore(user.getOtpLastSentAt().plusSeconds(RESEND_COOLDOWN_SECONDS));
    }

    public long getResendRemainingSeconds(User user) {
        if (user == null || user.getOtpLastSentAt() == null) return 0;
        long remaining = Duration.between(
                LocalDateTime.now(),
                user.getOtpLastSentAt().plusSeconds(RESEND_COOLDOWN_SECONDS)
        ).getSeconds();
        return Math.max(0, remaining);
    }

    public void setOtp(User user, String otp) {
        user.setOtp(hashOtp(otp));
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        user.setOtpAttempts(0);
        user.setOtpLastSentAt(LocalDateTime.now());
    }

    public boolean validateOtp(User user, String providedOtp) {
        if (user == null || providedOtp == null || providedOtp.isBlank()) return false;
        if (Boolean.TRUE.equals(user.getEmailVerified())) return false;
        if (user.getOtp() == null || user.getOtpExpiresAt() == null) return false;

        int attempts = user.getOtpAttempts() == null ? 0 : user.getOtpAttempts();
        if (attempts >= MAX_OTP_ATTEMPTS) return false;
        if (LocalDateTime.now().isAfter(user.getOtpExpiresAt())) return false;

        user.setOtpAttempts(attempts + 1);
        boolean valid = passwordEncoder.matches(providedOtp.trim(), user.getOtp());
        userRepository.save(user);
        return valid;
    }

    public void markEmailVerified(User user) {
        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiresAt(null);
        user.setOtpAttempts(0);
        user.setOtpLastSentAt(null);
        userRepository.save(user);
    }
}
