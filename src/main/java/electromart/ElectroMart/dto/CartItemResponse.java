package electromart.ElectroMart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long productId;

    private String title;

    private String brand;

    private String image;
    private String imageUrl;

    private Double price;
    private Double oldPrice;

    private Integer quantity;

    private Double lineTotal;
}

