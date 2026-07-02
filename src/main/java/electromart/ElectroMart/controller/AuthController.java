package electromart.ElectroMart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import electromart.ElectroMart.config.JwtUtil;
import electromart.ElectroMart.dto.LoginRequest;
import electromart.ElectroMart.dto.LoginResponse;
import electromart.ElectroMart.dto.RegisterRequest;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.service.UserService;
import electromart.ElectroMart.service.EmailService;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
             
          if(userService.findByEmail(request.getEmail()) != null){
        throw new RuntimeException("Email already exists");
    }



        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role("USER")
                .build();

      return userService.registerUser(user);
    }

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
@PostMapping("/login")
public LoginResponse login(
        @RequestBody LoginRequest request) {

    User user =
            userService.findByEmail(
                    request.getEmail());

    if (user == null) {
        throw new RuntimeException(
                "User not found");
    }

    if (!passwordEncoder.matches(
            request.getPassword(),
            user.getPassword())) {

        throw new RuntimeException(
                "Invalid Password");
    }

    String token =
            jwtUtil.generateToken(
                    user.getEmail());

    return new LoginResponse(
            token,
            user
    );
}

    @PostMapping("/send-otp")
    public Map<String, String> sendOtp(@RequestBody RegisterRequest request) {
        if (userService.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }

        String otp = userService.generateOtp();
        
        try {
            emailService.sendOtpEmail(request.getEmail(), otp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP: " + e.getMessage());
        }

        // Store OTP temporarily (in production, use Redis)
        User tempUser = User.builder()
                .email(request.getEmail())
                .otp(otp)
                .otpExpiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        userService.save(tempUser);

        return Map.of("message", "OTP sent to " + request.getEmail());
    }

    @PostMapping("/verify-otp")
    public Map<String, String> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String name = request.get("name");
        String password = request.get("password");

        User tempUser = userService.findByEmail(email);
        if (tempUser == null) {
            throw new RuntimeException("User not found. Send OTP first.");
        }

        if (!userService.validateOtp(tempUser, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(password)
                .role("USER")
                .emailVerified(true)
                .build();

        User registeredUser = userService.registerUser(user);
        return Map.of("message", "Email verified. You can now login.");
    }

    @PostMapping("/resend-otp")
    public Map<String, String> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = userService.generateOtp();
        
        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to resend OTP: " + e.getMessage());
        }

        User tempUser = userService.findByEmail(email);
        if (tempUser != null) {
            tempUser.setOtp(otp);
            tempUser.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
            userService.save(tempUser);
        }

        return Map.of("message", "OTP resent to " + email);
    }
}
