package electromart.ElectroMart.controller;

import electromart.ElectroMart.config.JwtUtil;
import electromart.ElectroMart.dto.LoginRequest;
import electromart.ElectroMart.dto.LoginResponse;
import electromart.ElectroMart.dto.RegisterRequest;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterRequest request) {
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

        String email = userService.normalizeEmail(request.getEmail());
        User existing = userService.findByEmail(email);
        if (existing != null) {
            throw new RuntimeException("Email already exists. Please login.");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .emailVerified(true)
                .build();
        userService.save(user);

        return Map.of("message", "Registration successful. You can now login.");
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

        String role = user.getRole();
        if (role == null || role.isBlank()) {
            role = "USER";
            user.setRole(role);
            userService.save(user);
        }

        // Simple email/password authentication; OTP verification is intentionally disabled.
        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token, user);
    }

    @GetMapping("/me")
    public User me() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new RuntimeException("Unauthorized");
        return userService.findByEmail(String.valueOf(auth.getPrincipal()));
    }
}
