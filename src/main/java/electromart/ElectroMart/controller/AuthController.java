package electromart.ElectroMart.controller;

import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/clerk/sync")
    public ResponseEntity<User> syncClerkUser(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String email = String.valueOf(authentication.getPrincipal()).trim().toLowerCase();
        String clerkUserId = String.valueOf(authentication.getCredentials());

        if (email.isBlank() || clerkUserId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String requestedEmail = body.get("email");
        String requestedName = body.get("name");

        if (requestedEmail == null || !email.equals(requestedEmail.trim().toLowerCase())) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.findByClerkUserId(clerkUserId);
        if (user == null) {
            user = userService.findByEmail(email);
        }

        if (user == null) {
            user = User.builder()
                    .name(requestedName == null || requestedName.isBlank() ? "ElectroMart User" : requestedName.trim())
                    .email(email)
                    .role("USER")
                    .emailVerified(true)
                    .clerkUserId(clerkUserId)
                    .build();
        } else {
            user.setClerkUserId(clerkUserId);
            if (requestedName != null && !requestedName.isBlank()) {
                user.setName(requestedName.trim());
            }
            user.setEmailVerified(true);
        }

        return ResponseEntity.ok(userService.save(user));
    }

    @GetMapping("/me")
    public User me(Authentication authentication) {
        return userService.findByEmail(String.valueOf(authentication.getPrincipal()));
    }
}
