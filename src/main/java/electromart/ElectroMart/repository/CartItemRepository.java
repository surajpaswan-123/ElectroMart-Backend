package electromart.ElectroMart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import electromart.ElectroMart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findAllByCartIdOrderByIdAsc(Long cartId);

    void deleteByCartId(Long cartId);
}
