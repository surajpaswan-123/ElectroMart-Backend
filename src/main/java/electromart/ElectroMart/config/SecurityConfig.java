package electromart.ElectroMart.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(clerkJwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> clerkJwtAuthenticationConverter(UserRepository userRepository) {
        return jwt -> {
            String email = jwt.getClaimAsString("email");
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException(
                    "Clerk session token is missing the 'email' custom claim"
                );
            }

            String role = "USER";
            User localUser = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
            if (localUser != null && localUser.getRole() != null && !localUser.getRole().isBlank()) {
                role = localUser.getRole();
            }

            return new UsernamePasswordAuthenticationToken(
                email.trim().toLowerCase(),
                jwt.getSubject(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
