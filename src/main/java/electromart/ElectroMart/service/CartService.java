package electromart.ElectroMart.service;

import java.util.List;

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
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new RuntimeException("Unauthorized");
        }

        String email = String.valueOf(auth.getPrincipal());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Unauthorized"));
        return user;
    }

    private Cart getOrCreateCartForUser(User user) {
        return cartRepository.findByUser(user).orElseGet(() ->
                cartRepository.save(Cart.builder().user(user).build())
        );
    }

    public CartResponse getCartForAuthenticatedUser() {
        return toCartResponse(getOrCreateCartForUser(getAuthenticatedUser()));
    }

    @Transactional
    public CartResponse addToCart(Long productId, Integer quantity) {
        validateQuantity(quantity);

        Product product = getProduct(productId);
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);
        CartItem existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElse(null);

        int requestedQuantity = quantity + (existing == null ? 0 : existing.getQuantity());
        validateStock(product, requestedQuantity);

        if (existing == null) {
            existing = CartItem.builder().cart(cart).product(product).quantity(quantity).build();
        } else {
            existing.setQuantity(requestedQuantity);
        }
        cartItemRepository.save(existing);
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long productId, Integer quantity) {
        validateQuantity(quantity);

        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item Not Found"));

        validateStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long productId) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);
        cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .ifPresent(cartItemRepository::delete);
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse clearCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);
        cartItemRepository.deleteByCartId(cart.getId());
        return toCartResponse(cart);
    }

    private Product getProduct(Long productId) {
        if (productId == null) {
            throw new RuntimeException("Product ID is required");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
        if (quantity > 100) {
            throw new RuntimeException("Quantity cannot exceed 100");
        }
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (product.getStock() == null || product.getStock() < 0) {
            throw new RuntimeException("Product stock is unavailable");
        }
        if (product.getStock() == 0) {
            throw new RuntimeException("Product Out Of Stock");
        }
        if (requestedQuantity > product.getStock()) {
            throw new RuntimeException("Only " + product.getStock() + " item(s) available");
        }
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findAllByCartIdOrderByIdAsc(cart.getId());

        List<CartItemResponse> responseItems = items.stream().map(ci -> {
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
        }).toList();

        int itemCount = responseItems.stream().mapToInt(CartItemResponse::getQuantity).sum();
        double subtotal = responseItems.stream().mapToDouble(CartItemResponse::getLineTotal).sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(responseItems)
                .itemCount(itemCount)
                .subtotal(subtotal)
                .total(subtotal)
                .build();
    }
}
