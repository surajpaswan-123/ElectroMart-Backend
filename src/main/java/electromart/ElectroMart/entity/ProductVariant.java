package electromart.ElectroMart.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variants",
       uniqueConstraints = @UniqueConstraint(name = "uk_product_variant_sku", columnNames = "sku"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductVariant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String color;

    @Column(length = 100)
    private String size;

    @Column(length = 100)
    private String storage;

    private Double price;
    private Double oldPrice;
    private Integer stock;

    @Builder.Default
    private Boolean active = true;
}
