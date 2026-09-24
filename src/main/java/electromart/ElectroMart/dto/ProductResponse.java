package electromart.ElectroMart.dto;

import java.util.List;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private Double oldPrice;
    private Integer stock;
    private String brand;
    private String image;
    private String imageUrl;
    private Double reviews;
    private Double rating;
    private CategoryResponse category;
    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
}