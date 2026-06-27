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
}