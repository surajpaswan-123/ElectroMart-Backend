package electromart.ElectroMart.service;

import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import electromart.ElectroMart.dto.CategoryResponse;
import electromart.ElectroMart.dto.ProductImageResponse;
import electromart.ElectroMart.dto.ProductVariantResponse;
import electromart.ElectroMart.dto.ProductResponse;
import electromart.ElectroMart.entity.Product;
import electromart.ElectroMart.entity.ProductImage;
import electromart.ElectroMart.entity.ProductVariant;
import electromart.ElectroMart.repository.ProductRepository;

@Service
public class ProductService {
    @Autowired private ProductRepository productRepository;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::toProductResponse).toList();
    }

    public Page<ProductResponse> searchProducts(String q, Long categoryId, String brand,
                                                Double minPrice, Double maxPrice, Double minRating,
                                                Boolean inStock, Pageable pageable) {
        return productRepository.search(q, categoryId, brand, minPrice, maxPrice, minRating, inStock, pageable)
                .map(this::toProductResponse);
    }

    public Product saveProduct(Product product) {
        if (product.getPrice() == null || product.getPrice() < 0) {
            throw new IllegalArgumentException("Product price must be zero or greater");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new IllegalArgumentException("Product stock must be zero or greater");
        }
        if (product.getImages() != null) {\n            product.getImages().forEach(image -> image.setProduct(product));\n        }\n        if (product.getVariants() != null) {\n            product.getVariants().forEach(variant -> {\n                variant.setProduct(product);\n                if (variant.getStock() != null && variant.getStock() < 0) throw new IllegalArgumentException("Variant stock must be zero or greater");\n                if (variant.getPrice() != null && variant.getPrice() < 0) throw new IllegalArgumentException("Variant price must be zero or greater");\n            });\n        }\n        return productRepository.save(product);
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
        return ProductResponse.builder()
                .id(product.getId()).title(product.getTitle()).description(product.getDescription())
                .price(product.getPrice()).oldPrice(product.getOldPrice()).stock(product.getStock())
                .brand(product.getBrand()).image(product.getImageUrl()).imageUrl(product.getImageUrl())
                .rating(product.getRating()).reviews(null).category(categoryResponse)
                .images(product.getImages() == null ? List.of() : product.getImages().stream().map(i -> ProductImageResponse.builder().id(i.getId()).imageUrl(i.getImageUrl()).displayOrder(i.getDisplayOrder()).primaryImage(i.getPrimaryImage()).build()).toList())
                .variants(product.getVariants() == null ? List.of() : product.getVariants().stream().map(v -> ProductVariantResponse.builder().id(v.getId()).sku(v.getSku()).name(v.getName()).color(v.getColor()).size(v.getSize()).storage(v.getStorage()).price(v.getPrice()).oldPrice(v.getOldPrice()).stock(v.getStock()).active(v.getActive()).build()).toList())
                .build();
    }
}