package electromart.ElectroMart.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
@JoinColumn(name = "category_id")
private Category category;

    private String title;

    private String description;

    private Double price;

    private Double oldPrice;

    private Integer stock;

    private String brand;

    private String imageUrl;

    private Double rating;
}