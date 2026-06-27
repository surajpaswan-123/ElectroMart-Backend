package electromart.ElectroMart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import electromart.ElectroMart.dto.CategoryResponse;
import electromart.ElectroMart.dto.ProductResponse;
import electromart.ElectroMart.entity.Product;
import electromart.ElectroMart.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toProductResponse)
                .toList();
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));
        return toProductResponse(product);
    }

    private ProductResponse toProductResponse(@NonNull Product product) {
        CategoryResponse categoryResponse = null;
        if (product.getCategory() != null) {
            categoryResponse = CategoryResponse.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .imageUrl(product.getCategory().getImageUrl())
                    .build();
        }

        // Centralized mapping to keep a stable frontend contract.
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .oldPrice(product.getOldPrice())
                .stock(product.getStock())
                .brand(product.getBrand())
                .image(product.getImageUrl())
                .imageUrl(product.getImageUrl())
                .rating(product.getRating())
                // Reviews are not present on entity; keep deterministic null.
                .reviews(null)
                .category(categoryResponse)
                .build();
    }
}

