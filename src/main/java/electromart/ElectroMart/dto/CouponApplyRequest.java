package electromart.ElectroMart.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponApplyRequest {
    private String code;
}
