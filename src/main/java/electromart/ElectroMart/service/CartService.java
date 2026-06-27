package electromart.ElectroMart.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import electromart.ElectroMart.dto.CartItemResponse;
import electromart.ElectroMart.dto.CartResponse;
import electromart.ElectroMart.entity.Cart;
import electromart.ElectroMart.entity.CartItem;
import electromart.ElectroMart.entity.Product;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.repository.CartItemRepository;
import electromart.ElectroMart.repository.CartRepository;
import electromart.ElectroMart.repository.ProductRepository;
import electromart.ElectroMart.repository.UserRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        String email = String.valueOf(auth.getPrincipal());
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new RuntimeException("Unauthorized");
        }
        return user;
    }

    private Cart getOrCreateCartForUser(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(cart);
                });
    }


    public CartResponse getCartForAuthenticatedUser() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be > 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        // Out-of-stock validation (if stock column is maintained)
        if (product.getStock() != null && product.getStock() <= 0) {
            throw new RuntimeException("Product Out Of Stock");
        }

        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);

        // Find existing cart item by cart+product (repository currently lacks query helpers)
        CartItem existing = cartItemRepository.findAll().stream()
                .filter(ci -> Objects.equals(ci.getCart().getId(), cart.getId())
                        && Objects.equals(ci.getProduct().getId(), productId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartItemRepository.save(existing);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(cartItem);
        }

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be > 0");
        }

        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);

        CartItem item = cartItemRepository.findAll().stream()
                .filter(ci -> Objects.equals(ci.getCart().getId(), cart.getId())
                        && Objects.equals(ci.getProduct().getId(), productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item Not Found"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long productId) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);

        cartItemRepository.findAll().stream()
                .filter(ci -> Objects.equals(ci.getCart().getId(), cart.getId())
                        && Objects.equals(ci.getProduct().getId(), productId))
                .findFirst()
                .ifPresent(ci -> cartItemRepository.deleteById(ci.getId()));

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse clearCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);

        cartItemRepository.findAll().stream()
                .filter(ci -> Objects.equals(ci.getCart().getId(), cart.getId()))
                .forEach(ci -> cartItemRepository.deleteById(ci.getId()));

        return toCartResponse(cart);
    }

    private CartResponse toCartResponse(Cart cart) {
        // Entity cart currently has cartItems as mappedBy, but it may be null if not initialized.
        List<CartItem> items = cartItemRepository.findAll().stream()
                .filter(ci -> ci.getCart() != null && Objects.equals(ci.getCart().getId(), cart.getId()))
                .toList();

        List<CartItemResponse> responseItems = items.stream()
                .map(ci -> {
                    Product p = ci.getProduct();
                    double price = p.getPrice() == null ? 0.0 : p.getPrice();
                    double lineTotal = price * ci.getQuantity();
                    return CartItemResponse.builder()
                            .productId(p.getId())
                            .title(p.getTitle())
                            .brand(p.getBrand())
                            .image(p.getImageUrl())
                            .imageUrl(p.getImageUrl())
                            .price(p.getPrice())
                            .oldPrice(p.getOldPrice())
                            .quantity(ci.getQuantity())
                            .lineTotal(lineTotal)
                            .build();
                })
                .toList();

        int itemCount = responseItems.stream().mapToInt(CartItemResponse::getQuantity).sum();
        double subtotal = responseItems.stream().mapToDouble(CartItemResponse::getLineTotal).sum();
        double total = subtotal; // shipping/tax are frontend-calculated in current UI; keep backend minimal.

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(responseItems)
                .itemCount(itemCount)
                .subtotal(subtotal)
                .total(total)
                .build();
    }
}
