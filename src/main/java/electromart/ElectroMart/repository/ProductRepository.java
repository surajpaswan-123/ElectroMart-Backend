package electromart.ElectroMart.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import electromart.ElectroMart.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
        SELECT p FROM Product p
        WHERE (:q IS NULL OR :q = ''
            OR LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(COALESCE(p.brand, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:brand IS NULL OR :brand = '' OR LOWER(p.brand) = LOWER(:brand))
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:minRating IS NULL OR COALESCE(p.rating, 0) >= :minRating)
        AND (:inStock IS NULL OR :inStock = false OR COALESCE(p.stock, 0) > 0)
        """)
    Page<Product> search(
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minRating") Double minRating,
            @Param("inStock") Boolean inStock,
            Pageable pageable);

    List<Product> findByBrandIgnoreCase(String brand);
}