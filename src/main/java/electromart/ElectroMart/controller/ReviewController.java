package electromart.ElectroMart.controller;

import electromart.ElectroMart.dto.ReviewRequest;
import electromart.ElectroMart.dto.ReviewResponse;
import electromart.ElectroMart.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewResponse> getReviews(@PathVariable Long productId) {
        return reviewService.getProductReviews(productId);
    }

    @GetMapping("/mine")
    public ResponseEntity<ReviewResponse> getMyReview(@PathVariable Long productId) {
        ReviewResponse review = reviewService.getMyReview(productId);
        return review == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(review);
    }

    @PostMapping
    public ReviewResponse createOrUpdate(@PathVariable Long productId,
                                         @RequestBody ReviewRequest request) {
        return reviewService.createOrUpdate(productId, request);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId,
                                       @PathVariable Long reviewId) {
        reviewService.delete(reviewId);
        return ResponseEntity.noContent().build();
    }
}
