package electromart.ElectroMart.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private String sku;
    private String name;
    private String color;
    private String size;
    private String storage;
    private Double price;
    private Double oldPrice;
    private Integer stock;
    private Boolean active;
}
