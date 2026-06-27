package electromart.ElectroMart.repository;

import electromart.ElectroMart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}