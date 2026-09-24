package electromart.ElectroMart.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import electromart.ElectroMart.dto.CouponApplyRequest;
import electromart.ElectroMart.dto.CouponRequest;
import electromart.ElectroMart.dto.CouponResponse;
import electromart.ElectroMart.entity.Cart;
import electromart.ElectroMart.entity.Coupon;
import electromart.ElectroMart.entity.Product;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.repository.CartItemRepository;
import electromart.ElectroMart.repository.CartRepository;
import electromart.ElectroMart.repository.CategoryRepository;
import electromart.ElectroMart.repository.CouponRepository;
import electromart.ElectroMart.repository.ProductRepository;
import electromart.ElectroMart.repository.UserRepository;

@Service
public class CouponService {

    @Autowired private CouponRepository couponRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    public Coupon create(CouponRequest request) {
        validateRequest(request);
        if (couponRepository.findByCodeIgnoreCase(request.getCode().trim()).isPresent()) {
            throw new RuntimeException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(normalize(request.getCode()))
                .discountType(request.getDiscountType().trim().toUpperCase(Locale.ROOT))
                .discountValue(request.getDiscountValue())
                .minimumOrderValue(request.getMinimumOrderValue())
                .maximumDiscount(request.getMaximumDiscount())
                .startsAt(request.getStartsAt())
                .expiresAt(request.getExpiresAt())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .active(request.getActive() == null || request.getActive())
                .build();

        if (request.getProductId() != null) {
            coupon.setProduct(productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product Not Found")));
        }
        if (request.getCategoryId() != null) {
            coupon.setCategory(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category Not Found")));
        }
        return couponRepository.save(coupon);
    }

    public CouponResponse apply(CouponApplyRequest request) {
        Cart cart = getCartForUser();
        Coupon coupon = findValidCoupon(request.getCode());
        double subtotal = cartItemRepository.findAllByCartIdOrderByIdAsc(cart.getId()).stream()
                .mapToDouble(item -> (item.getProduct().getPrice() == null ? 0.0 : item.getProduct().getPrice()) * item.getQuantity())
                .sum();

        validateMinimum(coupon, subtotal);
        validateScope(coupon, cart);

        double discount = calculateDiscount(coupon, subtotal);
        return CouponResponse.builder()
                .code(coupon.getCode())
                .cartSubtotal(round(subtotal))
                .discountAmount(round(discount))
                .finalAmount(round(Math.max(0, subtotal - discount)))
                .message("Coupon applied successfully")
                .build();
    }

    public Coupon findValidCoupon(String code) {
        if (code == null || code.isBlank()) throw new RuntimeException("Coupon code is required");
        Coupon coupon = couponRepository.findByCodeIgnoreCase(normalize(code))
                .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(coupon.getActive())) throw new RuntimeException("Coupon is inactive");
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) throw new RuntimeException("Coupon is not active yet");
        if (coupon.getExpiresAt() != null && now.isAfter(coupon.getExpiresAt())) throw new RuntimeException("Coupon has expired");
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) throw new RuntimeException("Coupon usage limit reached");
        return coupon;
    }

    @Transactional
    public void incrementUsage(Coupon coupon) {
        Coupon managed = couponRepository.findById(coupon.getId())
                .orElseThrow(() -> new RuntimeException("Coupon Not Found"));
        if (managed.getUsageLimit() != null && managed.getUsedCount() >= managed.getUsageLimit()) {
            throw new RuntimeException("Coupon usage limit reached");
        }
        managed.setUsedCount(managed.getUsedCount() + 1);
        couponRepository.save(managed);
    }

    private Cart getCartForUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new RuntimeException("Unauthorized");
        User user = userRepository.findByEmail(String.valueOf(auth.getPrincipal()))
                .orElseThrow(() -> new RuntimeException("Unauthorized"));
        return cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Cart is empty"));
    }

    private void validateMinimum(Coupon coupon, double subtotal) {
        if (coupon.getMinimumOrderValue() != null && subtotal < coupon.getMinimumOrderValue()) {
            throw new RuntimeException("Minimum order value is ₹" + coupon.getMinimumOrderValue());
        }
    }

    private void validateScope(Coupon coupon, Cart cart) {
        if (coupon.getProduct() == null && coupon.getCategory() == null) return;
        boolean matches = cartItemRepository.findAllByCartIdOrderByIdAsc(cart.getId()).stream().anyMatch(item -> {
            Product p = item.getProduct();
            boolean productMatch = coupon.getProduct() != null && p.getId().equals(coupon.getProduct().getId());
            boolean categoryMatch = coupon.getCategory() != null && p.getCategory() != null
                    && p.getCategory().getId().equals(coupon.getCategory().getId());
            return productMatch || categoryMatch;
        });
        if (!matches) throw new RuntimeException("Coupon is not applicable to items in your cart");
    }

    private double calculateDiscount(Coupon coupon, double subtotal) {
        double discount;
        if ("PERCENTAGE".equals(coupon.getDiscountType())) {
            discount = subtotal * coupon.getDiscountValue() / 100.0;
        } else {
            discount = coupon.getDiscountValue();
        }
        if (coupon.getMaximumDiscount() != null) discount = Math.min(discount, coupon.getMaximumDiscount());
        return Math.min(Math.max(discount, 0), subtotal);
    }

    private void validateRequest(CouponRequest r) {
        if (r == null || r.getCode() == null || r.getCode().isBlank()) throw new RuntimeException("Coupon code is required");
        if (r.getDiscountType() == null || (!"PERCENTAGE".equalsIgnoreCase(r.getDiscountType()) && !"FIXED".equalsIgnoreCase(r.getDiscountType()))) {
            throw new RuntimeException("Discount type must be PERCENTAGE or FIXED");
        }
        if (r.getDiscountValue() == null || r.getDiscountValue() <= 0) throw new RuntimeException("Discount value must be greater than 0");
        if ("PERCENTAGE".equalsIgnoreCase(r.getDiscountType()) && r.getDiscountValue() > 100) throw new RuntimeException("Percentage cannot exceed 100");
        if (r.getMinimumOrderValue() != null && r.getMinimumOrderValue() < 0) throw new RuntimeException("Minimum order value cannot be negative");
        if (r.getMaximumDiscount() != null && r.getMaximumDiscount() < 0) throw new RuntimeException("Maximum discount cannot be negative");
        if (r.getusageLimitSafe() != null) { }
    }

    private String normalize(String code) { return code.trim().toUpperCase(Locale.ROOT); }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
