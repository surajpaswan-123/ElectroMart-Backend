package electromart.ElectroMart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Product API response contract consumed by the frontend.
 *
 * Note: this is intentionally aligned with existing React components:
 * - ProductCard expects: id, brand, title, image, price, oldPrice, reviews, rating (optional)
 * - ProductDetails expects: imageUrl, category.name, price, oldPrice, description
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;

    private String title;

    private String description;

    private Double price;

    private Double oldPrice;

    private Integer stock;

    private String brand;

    /**
     * Frontend ProductCard expects this prop name.
     */
    private String image;

    /**
     * Frontend ProductDetails expects imageUrl.
     */
    private String imageUrl;

    /**
     * Frontend ProductCard expects reviews.
     */
    private Double reviews;

    /**
     * Frontend can also display rating.
     */
    private Double rating;

    private CategoryResponse category;
}

