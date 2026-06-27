package electromart.ElectroMart.dto;

import electromart.ElectroMart.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private User user;
}