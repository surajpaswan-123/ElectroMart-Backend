package electromart.ElectroMart.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private String code;
    private Double cartSubtotal;
    private Double discountAmount;
    private Double finalAmount;
    private String message;
}
