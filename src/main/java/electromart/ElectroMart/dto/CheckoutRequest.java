package electromart.ElectroMart.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckoutRequest {
    private String customerName;
    private String email;
    private String phone;
    private String address;
    private String paymentMethod;
    private String couponCode;
}
