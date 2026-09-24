package electromart.ElectroMart.repository;

import electromart.ElectroMart.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByProductIdOrderByCreatedAtDesc(Long productId);
    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);
    long countByProductId(Long productId);
    Double averageRatingByProductId(Long productId);
}
