package electromart.ElectroMart.service;

import electromart.ElectroMart.dto.ReviewRequest;
import electromart.ElectroMart.dto.ReviewResponse;
import electromart.ElectroMart.entity.Product;
import electromart.ElectroMart.entity.Review;
import electromart.ElectroMart.entity.User;
import electromart.ElectroMart.repository.ProductRepository;
import electromart.ElectroMart.repository.ReviewRepository;
import electromart.ElectroMart.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReviewResponse createOrUpdate(Long productId, ReviewRequest request) {
        if (productId == null || request == null || request.getRating() == null
                || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        String comment = request.getComment() == null ? "" : request.getComment().trim();
        if (comment.length() < 3 || comment.length() > 2000) {
            throw new IllegalArgumentException("Review comment must be between 3 and 2000 characters");
        }

        User user = getAuthenticatedUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Optional<Review> existing = reviewRepository.findByProductIdAndUserId(productId, user.getId());
        Review review = existing.orElseGet(Review::new);

        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(comment);
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }

        Review saved = reviewRepository.save(review);
        refreshProductRating(product);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(Long productId) {
        return reviewRepository.findAllByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewResponse getMyReview(Long productId) {
        User user = getAuthenticatedUser();
        return reviewRepository.findByProductIdAndUserId(productId, user.getId())
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public void delete(Long reviewId) {
        User user = getAuthenticatedUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can delete only your own review");
        }

        Product product = review.getProduct();
        reviewRepository.delete(review);
        reviewRepository.flush();
        refreshProductRating(product);
    }

    private void refreshProductRating(Product product) {
        Double average = reviewRepository.averageRatingByProductId(product.getId());
        product.setRating(average == null ? 0.0 : Math.round(average * 100.0) / 100.0);
        productRepository.save(product);
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return userRepository.findByEmail(String.valueOf(auth.getPrincipal()))
                .orElseThrow(() -> new IllegalArgumentException("Unauthorized"));
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userName(review.getUser().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
