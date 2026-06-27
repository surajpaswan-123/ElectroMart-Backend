package electromart.ElectroMart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import electromart.ElectroMart.entity.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByProductIdAndUserId(Long productId, Long userId);

    List<Wishlist> findAllByUserId(Long userId);
}

