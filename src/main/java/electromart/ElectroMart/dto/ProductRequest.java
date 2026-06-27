package electromart.ElectroMart.dto;

import lombok.Data;

@Data
public class ProductRequest {

    private String title;

    private String description;

    private Double price;

    private Double oldPrice;

    private Integer stock;

    private String brand;

    private String imageUrl;

    private Double rating;

    private Long categoryId;
}