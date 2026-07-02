package electromart.ElectroMart.service;

import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(User user) {
        user.setPassword(
            passwordEncoder.encode(user.getPassword())
        );
        return userRepository.save(user);
    }

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public boolean validateOtp(User user, String providedOtp) {
        if (user.getOtp() == null || user.getOtpExpiresAt() == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(user.getOtpExpiresAt())) {
            return false; // OTP expired
        }
        
        return user.getOtp().equals(providedOtp);
    }

    public void markEmailVerified(User user) {
        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);
    }
}
