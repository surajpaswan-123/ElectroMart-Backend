package electromart.ElectroMart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {

    private Long id; // wishlist row id

    private Long productId;

    // flattened product info for UI
    private String title;
    private String brand;
    private String image;
    private Double price;
    private Double oldPrice;

    private Double rating;
    private Integer reviews;
}

