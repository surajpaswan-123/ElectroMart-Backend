package electromart.ElectroMart.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import electromart.ElectroMart.dto.CheckoutRequest;
import electromart.ElectroMart.dto.CheckoutResponse;
import electromart.ElectroMart.entity.Cart;
import electromart.ElectroMart.entity.Order;
import electromart.ElectroMart.entity.OrderItem;
import electromart.ElectroMart.entity.Payment;
import electromart.ElectroMart.entity.Product;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.repository.CartItemRepository;
import electromart.ElectroMart.repository.CartRepository;
import electromart.ElectroMart.repository.OrderRepository;
import electromart.ElectroMart.repository.PaymentRepository;
import electromart.ElectroMart.repository.ProductRepository;
import electromart.ElectroMart.repository.UserRepository;

@Service
public class CheckoutService {

    private static final double SHIPPING = 20.0;

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private CouponService couponService;

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        validateRequest(request);

        User user = currentUser();
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Cart is empty"));
        List<var> cartItems = cartItemRepository.findAllByCartIdOrderByIdAsc(cart.getId());
        if (cartItems.isEmpty()) throw new RuntimeException("Cart is empty");

        double subtotal = 0;
        Order order = Order.builder()
                .customerName(request.getCustomerName().trim())
                .email(request.getEmail().trim())
                .phone(request.getPhone().trim())
                .address(request.getAddress().trim())
                .paymentMethod(request.getPaymentMethod().trim().toUpperCase())
                .orderStatus("PLACED")
                .orderDate(LocalDateTime.now())
                .user(user)
                .build();

        for (var cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product Not Found"));
            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getTitle());
            }
            double price = product.getPrice() == null ? 0 : product.getPrice();
            subtotal += price * cartItem.getQuantity();
            order.getOrderItems().add(OrderItem.builder()
                    .order(order).product(product).quantity(cartItem.getQuantity()).price(price).build());
        }

        double discount = 0;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            var coupon = couponService.findValidCoupon(request.getCouponCode());
            couponService.validateForCheckout(coupon, cart, subtotal);
            discount = couponService.calculateForCheckout(coupon, subtotal);
            order.setCouponCode(coupon.getCode());
            order.setDiscountAmount(discount);
        }

        double total = Math.max(0, subtotal + SHIPPING - discount);
        order.setTotalAmount(total);

        // Free-first demo payment: COD is completed immediately; card/UPI use a deterministic sandbox transaction.
        String method = order.getPaymentMethod();
        String transactionId = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String paymentStatus = "SUCCESS";

        Order saved = orderRepository.save(order);
        for (var item : saved.getOrderItems()) {
            Product p = item.getProduct();
            p.setStock(p.getStock() - item.getQuantity());
            productRepository.save(p);
        }

        Payment payment = Payment.builder()
                .order(saved).transactionId(transactionId).amount(total)
                .method(method).status(paymentStatus).createdAt(LocalDateTime.now()).build();
        paymentRepository.save(payment);

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            couponService.incrementUsage(couponService.findValidCoupon(request.getCouponCode()));
        }
        cartItemRepository.deleteByCartId(cart.getId());

        return CheckoutResponse.builder()
                .order(saved).transactionId(transactionId).paymentStatus(paymentStatus)
                .subtotal(round(subtotal)).discount(round(discount)).shipping(SHIPPING).total(round(total)).build();
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new RuntimeException("Unauthorized");
        return userRepository.findByEmail(String.valueOf(auth.getPrincipal()))
                .orElseThrow(() -> new RuntimeException("Unauthorized"));
    }

    private void validateRequest(CheckoutRequest r) {
        if (r == null) throw new RuntimeException("Checkout request is required");
        if (blank(r.getCustomerName()) || blank(r.getEmail()) || blank(r.getPhone()) || blank(r.getAddress()) || blank(r.getPaymentMethod())) {
            throw new RuntimeException("All checkout fields are required");
        }
        String method = r.getPaymentMethod().trim().toUpperCase();
        if (!List.of("COD", "UPI", "CREDIT CARD", "DEBIT CARD").contains(method)) {
            throw new RuntimeException("Unsupported payment method");
        }
    }

    private boolean blank(String s) { return s == null || s.isBlank(); }
    private double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
