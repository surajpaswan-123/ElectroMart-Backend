package electromart.ElectroMart.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ReviewResponse {
    Long id;
    Long productId;
    String userName;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
}
