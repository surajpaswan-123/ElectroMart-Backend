package electromart.ElectroMart.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import electromart.ElectroMart.entity.Order;
import electromart.ElectroMart.entity.OrderItem;
import electromart.ElectroMart.entity.Payment;
import electromart.ElectroMart.entity.Product;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.repository.OrderRepository;
import electromart.ElectroMart.repository.PaymentRepository;
import electromart.ElectroMart.repository.ProductRepository;
import electromart.ElectroMart.repository.UserRepository;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public Order placeOrder(Order order) {
        if (order == null) throw new IllegalArgumentException("Order payload is null");

        User user = currentUser();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus("PLACED");

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        double calculatedTotal = 0;
        for (OrderItem item : order.getOrderItems()) {
            if (item == null || item.getProduct() == null || item.getProduct().getId() == null) {
                throw new RuntimeException("Every order item must contain a product");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException("Order quantity must be greater than 0");
            }

            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product Not Found: " + item.getProduct().getId()));

            if (product.getStock() == null || product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getTitle());
            }

            double price = product.getPrice() == null ? 0 : product.getPrice();
            item.setProduct(product);
            item.setPrice(price);
            item.setOrder(order);
            calculatedTotal += price * item.getQuantity();
        }

        double discount = order.getDiscountAmount() == null ? 0 : Math.max(0, order.getDiscountAmount());
        double expectedTotal = Math.max(0, calculatedTotal + 20 - discount);
        order.setTotalAmount(round(expectedTotal));

        Order saved = orderRepository.save(order);

        for (OrderItem item : saved.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        return saved;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));
        User user = currentUser();
        if (!isAdmin(user) && !order.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access this order");
        }
        return order;
    }

    public List<Order> getMyOrders() {
        return orderRepository.findByUserOrderByOrderDateDesc(currentUser());
    }

    @Transactional
    public Order updateStatus(Long id, String status) {
        User admin = currentUser();
        if (!isAdmin(admin)) throw new org.springframework.security.access.AccessDeniedException("Admin access required");

        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!List.of("PLACED", "CONFIRMED", "PROCESSING", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED", "RETURN_APPROVED", "REFUNDED").contains(normalized)) {
            throw new RuntimeException("Invalid order status");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));
        order.setOrderStatus(normalized);
        return orderRepository.save(order);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new org.springframework.security.authentication.BadCredentialsException("Unauthorized");
        return userRepository.findByEmail(String.valueOf(auth.getPrincipal()))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
