package electromart.ElectroMart.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRequest {
    private String code;
    private String discountType;
    private Double discountValue;
    private Double minimumOrderValue;
    private Double maximumDiscount;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private Integer usageLimit;
    private Boolean active;
    private Long productId;
    private Long categoryId;
}
