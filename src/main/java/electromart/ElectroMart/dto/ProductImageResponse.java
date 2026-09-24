package electromart.ElectroMart.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean primaryImage;
}
