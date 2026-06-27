package electromart.ElectroMart.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import electromart.ElectroMart.entity.Order;
import electromart.ElectroMart.entity.OrderItem;
import electromart.ElectroMart.entity.Product;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.repository.OrderRepository;
import electromart.ElectroMart.repository.ProductRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    public Order placeOrder(Order order) {
        System.out.println("[OrderService] placeOrder() started");
        if (order == null) {
            throw new IllegalArgumentException("Order payload is null");
        }

        order.setOrderDate(LocalDateTime.now());

        if (order.getOrderStatus() == null) {
            order.setOrderStatus("PLACED");
        }

        if (order.getOrderItems() != null) {
            System.out.println("[OrderService] orderItems size=" + order.getOrderItems().size());
            for (OrderItem item : order.getOrderItems()) {
                Long productId = (item != null && item.getProduct() != null) ? item.getProduct().getId() : null;
                System.out.println("[OrderService] Processing OrderItem: productId=" + productId + ", qty=" + (item == null ? null : item.getQuantity()) + ", price=" + (item == null ? null : item.getPrice()));

                if (item.getProduct() != null && item.getProduct().getId() != null) {
                    Product managedProduct = productRepository.findById(item.getProduct().getId())
                            .orElseThrow(() -> new RuntimeException("Product Not Found: " + item.getProduct().getId()));

                    item.setProduct(managedProduct);

                    // Ensure price matches current product price if missing
                    if (item.getPrice() == null) {
                        item.setPrice(managedProduct.getPrice());
                    }
                }
                // Ensure bidirectional relationship
                item.setOrder(order);
            }
        } else {
            System.out.println("[OrderService] orderItems is null");
        }

        // Attach authenticated user (from JWT) before saving.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.authentication.BadCredentialsException("Unauthorized: missing/invalid JWT authentication");
        }

        String email = authentication.getPrincipal() == null ? null : authentication.getPrincipal().toString();
        if (email == null || email.isBlank()) {
            throw new org.springframework.security.authentication.BadCredentialsException("Unauthorized: JWT principal email is missing");
        }

        User authenticatedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + email));

        order.setUser(authenticatedUser);

        System.out.println("[OrderService] Saving order with user_id=" + authenticatedUser.getId());
        Order saved = orderRepository.save(order);
        System.out.println("[OrderService] Saved order. id=" + saved.getId());
        System.out.println("[OrderService] saved.orderItems size=" + (saved.getOrderItems() == null ? 0 : saved.getOrderItems().size()));

        return saved;
    }


    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));
    }

// OrderService.java — PURA getMyOrders() METHOD REPLACE KARO

@Autowired
private electromart.ElectroMart.repository.UserRepository userRepository;

public List<Order> getMyOrders() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new SecurityException("Unauthenticated");
    }

    String emailForOrders = authentication.getPrincipal().toString();

    User userForOrders = userRepository.findByEmail(emailForOrders)
            .orElseThrow(() -> new RuntimeException("User not found: " + emailForOrders));

    return orderRepository.findByUser(userForOrders);
}
}

