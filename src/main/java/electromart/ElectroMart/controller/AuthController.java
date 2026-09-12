package electromart.ElectroMart.controller;

import electromart.ElectroMart.config.JwtUtil;
import electromart.ElectroMart.dto.LoginRequest;
import electromart.ElectroMart.dto.LoginResponse;
import electromart.ElectroMart.dto.RegisterRequest;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.service.EmailService;
import electromart.ElectroMart.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        validateRegistrationRequest(request);
        return sendOtpInternal(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String email = userService.normalizeEmail(request.getEmail());
        User user = userService.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token, user);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody RegisterRequest request) {
        validateRegistrationRequest(request);
        return sendOtpInternal(request);
    }

    private ResponseEntity<Map<String, String>> sendOtpInternal(RegisterRequest request) {
        String email = userService.normalizeEmail(request.getEmail());
        User existing = userService.findByEmail(email);

        if (existing != null && Boolean.TRUE.equals(existing.getEmailVerified())) {
            throw new RuntimeException("Email already exists");
        }

        User pendingUser = existing;
        if (pendingUser == null) {
            pendingUser = User.builder()
                    .name(request.getName().trim())
                    .email(email)
                    .password(request.getPassword())
                    .role("USER")
                    .emailVerified(false)
                    .otpAttempts(0)
                    .build();
            pendingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            if (request.getName() != null && !request.getName().isBlank()) {
                pendingUser.setName(request.getName().trim());
            }
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                pendingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }

        if (!userService.canResendOtp(pendingUser)) {
            long remaining = userService.getResendRemainingSeconds(pendingUser);
            throw new RuntimeException("Please wait " + remaining + " seconds before requesting another OTP");
        }

        String otp = userService.generateOtp();
        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            log.error("OTP email delivery failed for {}: {}", maskEmail(email), e.getMessage(), e);
            throw new RuntimeException("OTP email could not be sent. Check the email service configuration and try again.");
        }

        userService.setOtp(pendingUser, otp);
        userService.save(pendingUser);

        return ResponseEntity.ok(Map.of("message", "OTP sent to " + email));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody Map<String, String> request) {
        String email = userService.normalizeEmail(request.get("email"));
        String otp = request.get("otp");

        if (email == null || email.isBlank() || otp == null || !otp.matches("\\d{6}")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Enter a valid email and 6-digit OTP"));
        }

        User pendingUser = userService.findByEmail(email);
        if (pendingUser == null) {
            throw new RuntimeException("User not found. Please register again.");
        }
        if (Boolean.TRUE.equals(pendingUser.getEmailVerified())) {
            throw new RuntimeException("Email is already verified");
        }

        if (!userService.validateOtp(pendingUser, otp)) {
            int attempts = pendingUser.getOtpAttempts() == null ? 0 : pendingUser.getOtpAttempts();
            if (attempts >= 5) {
                throw new RuntimeException("Too many incorrect OTP attempts. Please request a new OTP.");
            }
            throw new RuntimeException("Invalid or expired OTP");
        }

        userService.markEmailVerified(pendingUser);
        return ResponseEntity.ok(Map.of("message", "Email verified. You can now login."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestBody Map<String, String> request) {
        String email = userService.normalizeEmail(request.get("email"));
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email is required"));
        }

        User pendingUser = userService.findByEmail(email);
        if (pendingUser == null) {
            throw new RuntimeException("Registration not found. Please register again.");
        }
        if (Boolean.TRUE.equals(pendingUser.getEmailVerified())) {
            throw new RuntimeException("Email is already verified");
        }
        if (!userService.canResendOtp(pendingUser)) {
            long remaining = userService.getResendRemainingSeconds(pendingUser);
            throw new RuntimeException("Please wait " + remaining + " seconds before requesting another OTP");
        }

        String otp = userService.generateOtp();
        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            log.error("OTP resend failed for {}: {}", maskEmail(email), e.getMessage(), e);
            throw new RuntimeException("OTP email could not be resent. Check the email service configuration and try again.");
        }

        userService.setOtp(pendingUser, otp);
        userService.save(pendingUser);

        return ResponseEntity.ok(Map.of("message", "OTP resent to " + email));
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Name, email and password are required");
        }
        if (!request.getEmail().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Enter a valid email address");
        }
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String maskedLocal = local.length() <= 2
                ? "*"
                : local.substring(0, 1) + "***" + local.substring(local.length() - 1);
        return maskedLocal + "@" + parts[1];
    }
}
