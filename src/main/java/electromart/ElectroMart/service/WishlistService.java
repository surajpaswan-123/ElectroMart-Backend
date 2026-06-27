package electromart.ElectroMart.service;

import electromart.ElectroMart.dto.WishlistResponse;
import electromart.ElectroMart.entity.Product;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.entity.Wishlist;
import electromart.ElectroMart.repository.ProductRepository;
import electromart.ElectroMart.repository.UserRepository;
import electromart.ElectroMart.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public WishlistResponse addToWishlist(Long productId) {
        if (productId == null) {
            throw new RuntimeException("Invalid Product");
        }

        User user = getAuthenticatedUser();

        // Validate product existence
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Invalid Product"));

        // Prevent duplicate items per user
        Optional<Wishlist> existing = wishlistRepository.findByProductIdAndUserId(productId, user.getId());
        if (existing.isPresent()) {
            return toDto(existing.get());
        }

        Wishlist saved = wishlistRepository.save(
                Wishlist.builder()
                        .product(product)
                        .user(user)
                        .build()
        );

        return toDto(saved);
    }

    public List<WishlistResponse> getWishlist() {
        User user = getAuthenticatedUser();

        // Only items for authenticated user
        List<Wishlist> items = wishlistRepository.findAllByUserId(user.getId());

        // Remove invalid products gracefully
        List<Wishlist> valid = items.stream()
                .filter(w -> w.getProduct() != null && w.getProduct().getId() != null)
                .collect(Collectors.toList());

        // delete rows that reference missing/null products
        List<Wishlist> invalid = items.stream()
                .filter(w -> w.getProduct() == null || w.getProduct().getId() == null)
                .collect(Collectors.toList());
        invalid.forEach(wishlistRepository::delete);

        // validate product existence
        List<WishlistResponse> result = valid.stream()
                .filter(w -> productRepository.existsById(w.getProduct().getId()))
                .peek(w -> {
                    if (!productRepository.existsById(w.getProduct().getId())) {
                        wishlistRepository.delete(w);
                    }
                })
                .map(this::toDto)
                .collect(Collectors.toList());

        return result;
    }

    public void removeWishlist(Long productId) {
        if (productId == null) return;

        User user = getAuthenticatedUser();

        // Remove only the user's wishlist item
        Optional<Wishlist> existing = wishlistRepository.findByProductIdAndUserId(productId, user.getId());
        existing.ifPresent(wishlistRepository::delete);
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        String email = String.valueOf(auth.getPrincipal());
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Unauthorized"));
    }

    private WishlistResponse toDto(Wishlist w) {
        Product p = w.getProduct();

        return WishlistResponse.builder()
                .id(w.getId())
                .productId(p != null ? p.getId() : null)
                .title(p != null ? p.getTitle() : null)
                .brand(p != null ? p.getBrand() : null)
                .image(p != null ? p.getImageUrl() : null)
                .price(p != null ? p.getPrice() : null)
                .oldPrice(p != null ? p.getOldPrice() : null)
                .rating(p != null ? p.getRating() : null)
                .reviews(null)
                .build();
    }
}


